package com.docappoint.service;

import com.docappoint.dto.AppointmentCreate;
import com.docappoint.dto.BreakInterval;
import com.docappoint.dto.SlotGenerateRequest;
import com.docappoint.entity.Appointment;
import com.docappoint.entity.Role;
import com.docappoint.entity.Slot;
import com.docappoint.entity.Status;
import com.docappoint.entity.User;
import com.docappoint.repository.AppointmentRepository;
import com.docappoint.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentDomainTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private SlotRepository slotRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private SlotService slotService;

    private User doctor;
    private User otherDoctor;
    private User patient;
    private User otherPatient;

    @BeforeEach
    void setUp() {
        slotService = new SlotService(slotRepository);

        doctor = new User(1, "dr_smith", "1112223333", "pass", Role.doctor);
        otherDoctor = new User(2, "dr_jones", "4445556666", "pass", Role.doctor);
        patient = new User(3, "john_doe", "7778889999", "pass", Role.patient);
        otherPatient = new User(4, "jane_doe", "0001112222", "pass", Role.patient);
    }

    // --- Appointment Booking Tests ---

    @Test
    void testBookAppointmentSuccess() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        Slot slot = new Slot(10, doctor, futureDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.available);

        when(slotRepository.findByIdForUpdate(10)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsByPatientIdAndSlotDateAndStatus(patient.getId(), futureDate, Status.booked))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Appointment appointment = appointmentService.bookAppointment(patient, new AppointmentCreate(10));

        assertNotNull(appointment);
        assertEquals(Status.booked, appointment.getStatus());
        assertEquals(Status.booked, slot.getStatus());
        assertEquals(patient, appointment.getPatient());
        verify(slotRepository).save(slot);
    }

    @Test
    void testBookAppointmentRejectsNonPatient() {
        assertThrows(SecurityException.class, () ->
                appointmentService.bookAppointment(doctor, new AppointmentCreate(10))
        );
    }

    @Test
    void testBookAppointmentRejectsSlotNotFound() {
        when(slotRepository.findByIdForUpdate(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                appointmentService.bookAppointment(patient, new AppointmentCreate(99))
        );
    }

    @Test
    void testBookAppointmentRejectsPastSlot() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Slot slot = new Slot(10, doctor, pastDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.available);
        when(slotRepository.findByIdForUpdate(10)).thenReturn(Optional.of(slot));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.bookAppointment(patient, new AppointmentCreate(10))
        );
        assertTrue(ex.getMessage().contains("already passed"));
    }

    @Test
    void testBookAppointmentRejectsUnavailableSlot() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        Slot slot = new Slot(10, doctor, futureDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.booked);
        when(slotRepository.findByIdForUpdate(10)).thenReturn(Optional.of(slot));

        assertThrows(IllegalStateException.class, () ->
                appointmentService.bookAppointment(patient, new AppointmentCreate(10))
        );
    }

    @Test
    void testBookAppointmentRejectsDoubleBookingSameDate() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        Slot slot = new Slot(10, doctor, futureDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.available);
        when(slotRepository.findByIdForUpdate(10)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsByPatientIdAndSlotDateAndStatus(patient.getId(), futureDate, Status.booked))
                .thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.bookAppointment(patient, new AppointmentCreate(10))
        );
        assertTrue(ex.getMessage().contains("already has an active booked appointment"));
    }

    // --- Appointment Completion Tests ---

    @Test
    void testCompleteAppointmentSuccess() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Slot slot = new Slot(10, doctor, pastDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.booked);
        Appointment appointment = new Appointment(100, slot, patient, Status.booked, null);

        when(appointmentRepository.findById(100)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment completed = appointmentService.completeAppointment(doctor, 100);

        assertEquals(Status.completed, completed.getStatus());
        assertEquals(Status.completed, slot.getStatus());
        verify(slotRepository).save(slot);
    }

    @Test
    void testCompleteAppointmentRejectsNonDoctor() {
        assertThrows(SecurityException.class, () ->
                appointmentService.completeAppointment(patient, 100)
        );
    }

    @Test
    void testCompleteAppointmentRejectsDifferentDoctor() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Slot slot = new Slot(10, doctor, pastDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.booked);
        Appointment appointment = new Appointment(100, slot, patient, Status.booked, null);
        when(appointmentRepository.findById(100)).thenReturn(Optional.of(appointment));

        assertThrows(SecurityException.class, () ->
                appointmentService.completeAppointment(otherDoctor, 100)
        );
    }

    @Test
    void testCompleteAppointmentRejectsFutureAppointment() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Slot slot = new Slot(10, doctor, futureDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.booked);
        Appointment appointment = new Appointment(100, slot, patient, Status.booked, null);
        when(appointmentRepository.findById(100)).thenReturn(Optional.of(appointment));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.completeAppointment(doctor, 100)
        );
        assertTrue(ex.getMessage().contains("future"));
    }

    // --- Appointment Cancellation Tests ---

    @Test
    void testCancelAppointmentSuccess() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        Slot slot = new Slot(10, doctor, futureDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.booked);
        Appointment appointment = new Appointment(100, slot, patient, Status.booked, null);

        when(appointmentRepository.findById(100)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment cancelled = appointmentService.cancelAppointment(patient, 100);

        assertEquals(Status.cancelled, cancelled.getStatus());
        assertEquals(Status.available, slot.getStatus());
        verify(slotRepository).save(slot);
    }

    @Test
    void testCancelAppointmentRejectsNonOwner() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        Slot slot = new Slot(10, doctor, futureDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.booked);
        Appointment appointment = new Appointment(100, slot, patient, Status.booked, null);

        when(appointmentRepository.findById(100)).thenReturn(Optional.of(appointment));

        assertThrows(SecurityException.class, () ->
                appointmentService.cancelAppointment(otherPatient, 100)
        );
        assertThrows(SecurityException.class, () ->
                appointmentService.cancelAppointment(doctor, 100)
        );
    }

    @Test
    void testCancelAppointmentRejectsCompleted() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Slot slot = new Slot(10, doctor, pastDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.completed);
        Appointment appointment = new Appointment(100, slot, patient, Status.completed, null);

        when(appointmentRepository.findById(100)).thenReturn(Optional.of(appointment));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.cancelAppointment(patient, 100)
        );
        assertTrue(ex.getMessage().contains("completed"));
    }

    @Test
    void testCancelAppointmentRejectsPastAppointment() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Slot slot = new Slot(10, doctor, pastDate, LocalTime.of(10, 0), LocalTime.of(10, 30), Status.booked);
        Appointment appointment = new Appointment(100, slot, patient, Status.booked, null);

        when(appointmentRepository.findById(100)).thenReturn(Optional.of(appointment));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointmentService.cancelAppointment(patient, 100)
        );
        assertTrue(ex.getMessage().contains("already passed"));
    }

    // --- SlotService Tests ---

    @Test
    void testGenerateSlotsHandlesBreaksAndExistingSlots() {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(12, 0);
        BreakInterval lunchBreak = new BreakInterval(LocalTime.of(10, 0), LocalTime.of(11, 0));

        Slot existingSlot = new Slot(5, doctor, targetDate, LocalTime.of(9, 0), LocalTime.of(9, 30), Status.available);
        when(slotRepository.findByDoctorAndDateOrderByStartTimeAsc(doctor, targetDate))
                .thenReturn(List.of(existingSlot));

        when(slotRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        SlotGenerateRequest request = new SlotGenerateRequest(
                targetDate, start, end, 30, List.of(lunchBreak)
        );

        List<Slot> created = slotService.generateSlots(doctor, request);

        // Expected slots:
        // 09:00-09:30 overlaps existing slot -> skipped
        // 09:30-10:00 -> generated
        // 10:00-10:30 overlaps break (10:00-11:00) -> advanced to 11:00
        // 11:00-11:30 -> generated
        // 11:30-12:00 -> generated
        assertEquals(3, created.size());
        assertEquals(LocalTime.of(9, 30), created.get(0).getStartTime());
        assertEquals(LocalTime.of(11, 0), created.get(1).getStartTime());
        assertEquals(LocalTime.of(11, 30), created.get(2).getStartTime());
    }

    @Test
    void testToggleFreezeSlot() {
        Slot slot = new Slot(10, doctor, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.available);
        when(slotRepository.findByIdAndDoctor(10, doctor)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(Slot.class))).thenAnswer(inv -> inv.getArgument(0));

        Slot frozen = slotService.toggleFreezeSlot(doctor, 10);
        assertEquals(Status.frozen, frozen.getStatus());

        Slot thawed = slotService.toggleFreezeSlot(doctor, 10);
        assertEquals(Status.available, thawed.getStatus());
    }

    @Test
    void testToggleFreezeSlotRejectsBooked() {
        Slot slot = new Slot(10, doctor, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.booked);
        when(slotRepository.findByIdAndDoctor(10, doctor)).thenReturn(Optional.of(slot));

        assertThrows(IllegalStateException.class, () ->
                slotService.toggleFreezeSlot(doctor, 10)
        );
    }

    @Test
    void testGenerateSlotsStartsFromCurrentTimeForToday() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime start = now.minusMinutes(30);
        LocalTime end = now.plusMinutes(90);

        when(slotRepository.findByDoctorAndDateOrderByStartTimeAsc(doctor, today))
                .thenReturn(List.of());
        when(slotRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        SlotGenerateRequest request = new SlotGenerateRequest(today, start, end, 30, List.of());

        List<Slot> created = slotService.generateSlots(doctor, request);

        assertFalse(created.isEmpty());
        assertTrue(created.stream().allMatch(slot -> !slot.getStartTime().isBefore(now)));
    }
}
