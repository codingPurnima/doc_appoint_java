package com.docappoint.service;

import com.docappoint.dto.AppointmentCreate;
import com.docappoint.dto.DoctorAppointmentResponse;
import com.docappoint.entity.Appointment;
import com.docappoint.entity.Role;
import com.docappoint.entity.Slot;
import com.docappoint.entity.Status;
import com.docappoint.entity.User;
import com.docappoint.repository.AppointmentRepository;
import com.docappoint.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, SlotRepository slotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public Appointment bookAppointment(User patient, AppointmentCreate request) {
        if (patient.getRole() != Role.patient) {
            throw new SecurityException("Only patients are permitted to book appointments");
        }

        // Lock the slot using pessimistic write lock (SELECT ... FOR UPDATE) to prevent race conditions
        Slot slot = slotRepository.findByIdForUpdate(request.slotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found with ID: " + request.slotId()));

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalTime now = java.time.LocalTime.now();
        if (slot.getDate().isBefore(today) || (slot.getDate().isEqual(today) && slot.getStartTime().isBefore(now))) {
            throw new IllegalStateException("Cannot book an appointment for a slot that has already passed");
        }

        if (slot.getStatus() != Status.available) {
            throw new IllegalStateException("Slot is not available for booking (current status: " + slot.getStatus() + ")");
        }

        // Enforce one booked appointment per patient per date
        boolean alreadyBooked = appointmentRepository.existsByPatientIdAndSlotDateAndStatus(
                patient.getId(), slot.getDate(), Status.booked
        );
        if (alreadyBooked) {
            throw new IllegalStateException("Patient already has an active booked appointment on " + slot.getDate());
        }

        // Transition slot status to booked
        slot.setStatus(Status.booked);
        slotRepository.save(slot);

        // Create appointment record
        Appointment appointment = new Appointment(slot, patient, Status.booked);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment completeAppointment(User doctor, Integer appointmentId) {
        if (doctor.getRole() != Role.doctor) {
            throw new SecurityException("Only doctors can mark appointments as completed");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

        if (!appointment.getSlot().getDoctor().getId().equals(doctor.getId())) {
            throw new SecurityException("Unauthorized: Appointment belongs to another doctor's slot");
        }

        if (appointment.getStatus() != Status.booked) {
            throw new IllegalStateException("Only booked appointments can be completed");
        }

        Slot slot = appointment.getSlot();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalTime now = java.time.LocalTime.now();
        if (slot.getDate().isAfter(today) || (slot.getDate().isEqual(today) && slot.getStartTime().isAfter(now))) {
            throw new IllegalStateException("Cannot complete an appointment that is scheduled in the future");
        }

        appointment.setStatus(Status.completed);
        slot.setStatus(Status.completed);
        slotRepository.save(slot);

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getPatientAppointments(User patient) {
        return appointmentRepository.findByPatientOrderByIdDesc(patient);
    }

    public List<DoctorAppointmentResponse> getDoctorAppointments(User doctor) {
        List<Appointment> appointments = appointmentRepository.findBySlotDoctorOrderByIdDesc(doctor);
        return appointments.stream()
                .map(a -> new DoctorAppointmentResponse(
                        a.getId(),
                        a.getSlot().getDate(),
                        a.getSlot().getStartTime(),
                        a.getSlot().getEndTime(),
                        a.getStatus().name(),
                        a.getPatient().getName()
                ))
                .toList();
    }

    @Transactional
    public Appointment cancelAppointment(User currentUser, Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

        // Reproduce original FastAPI behavior: ONLY the patient who owns the appointment can cancel it
        if (currentUser.getRole() != Role.patient || !appointment.getPatient().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized: Only the patient who booked this appointment can cancel it");
        }

        if (appointment.getStatus() == Status.cancelled) {
            throw new IllegalStateException("Appointment is already cancelled");
        }
        if (appointment.getStatus() == Status.completed) {
            throw new IllegalStateException("Cannot cancel an already completed appointment");
        }

        Slot slot = appointment.getSlot();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalTime now = java.time.LocalTime.now();
        if (slot.getDate().isBefore(today) || (slot.getDate().isEqual(today) && slot.getStartTime().isBefore(now))) {
            throw new IllegalStateException("Cannot cancel an appointment that has already passed");
        }

        appointment.setStatus(Status.cancelled);

        // Restore the slot to available so another patient can book it
        slot.setStatus(Status.available);
        slotRepository.save(slot);

        return appointmentRepository.save(appointment);
    }
}
