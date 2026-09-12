package com.docappoint.controller;

import com.docappoint.dto.AppointmentCreate;
import com.docappoint.dto.DoctorAppointmentResponse;
import com.docappoint.entity.Appointment;
import com.docappoint.entity.User;
import com.docappoint.service.AppointmentService;
import com.docappoint.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthService authService;

    public AppointmentController(AppointmentService appointmentService, AuthService authService) {
        this.appointmentService = appointmentService;
        this.authService = authService;
    }

    @PostMapping("/book")
    public ResponseEntity<Appointment> book(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AppointmentCreate request) {
        User currentUser = getAuthenticatedUser(authHeader);
        return ResponseEntity.ok(appointmentService.bookAppointment(currentUser, request));
    }

    @PatchMapping("/{appointment_id}/complete")
    public ResponseEntity<Appointment> complete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("appointment_id") Integer appointmentId) {
        User currentUser = getAuthenticatedUser(authHeader);
        return ResponseEntity.ok(appointmentService.completeAppointment(currentUser, appointmentId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<DoctorAppointmentResponse>> getMyAppointments(
            @RequestHeader("Authorization") String authHeader) {
        User currentUser = getAuthenticatedUser(authHeader);
        List<Appointment> appointments = appointmentService.getPatientAppointments(currentUser);
        List<DoctorAppointmentResponse> response = appointments.stream()
                .map(a -> new DoctorAppointmentResponse(
                        a.getId(),
                        a.getSlot().getDate(),
                        a.getSlot().getStartTime(),
                        a.getSlot().getEndTime(),
                        a.getStatus().name(),
                        a.getPatient().getName()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor")
    public ResponseEntity<List<DoctorAppointmentResponse>> getDoctorAppointments(
            @RequestHeader("Authorization") String authHeader) {
        User currentUser = getAuthenticatedUser(authHeader);
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(currentUser));
    }

    @PutMapping("/{appointment_id}/cancel")
    public ResponseEntity<Appointment> cancel(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("appointment_id") Integer appointmentId) {
        User currentUser = getAuthenticatedUser(authHeader);
        return ResponseEntity.ok(appointmentService.cancelAppointment(currentUser, appointmentId));
    }

    private User getAuthenticatedUser(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new SecurityException("Missing Authorization header");
        }
        return authService.getUserFromToken(authHeader);
    }
}
