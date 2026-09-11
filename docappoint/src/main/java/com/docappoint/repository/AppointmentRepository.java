package com.docappoint.repository;

import com.docappoint.entity.Appointment;
import com.docappoint.entity.Slot;
import com.docappoint.entity.Status;
import com.docappoint.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Find appointments by patient ordered by id descending
    List<Appointment> findByPatientOrderByIdDesc(User patient);

    List<Appointment> findByPatientIdOrderByIdDesc(Integer patientId);

    // Find doctor appointments through the slot's doctor
    List<Appointment> findBySlotDoctorOrderByIdDesc(User doctor);

    List<Appointment> findBySlotDoctorIdOrderByIdDesc(Integer doctorId);

    // Find a patient's booked appointment for a particular date
    Optional<Appointment> findByPatientAndSlotDateAndStatus(User patient, LocalDate date, Status status);

    Optional<Appointment> findByPatientIdAndSlotDateAndStatus(Integer patientId, LocalDate date, Status status);

    boolean existsByPatientIdAndSlotDateAndStatus(Integer patientId, LocalDate date, Status status);

    // Find appointment by id and patient
    Optional<Appointment> findByIdAndPatient(Integer id, User patient);

    Optional<Appointment> findByIdAndPatientId(Integer id, Integer patientId);

    // Find appointment by slot
    Optional<Appointment> findBySlot(Slot slot);

    Optional<Appointment> findBySlotId(Integer slotId);
}
