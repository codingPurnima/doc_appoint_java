package com.docappoint.controller;

import com.docappoint.dto.AppointmentCreate;
import com.docappoint.dto.DoctorAppointmentResponse;
import com.docappoint.dto.RefreshRequest;
import com.docappoint.dto.SlotGenerateRequest;
import com.docappoint.dto.TokenResponse;
import com.docappoint.dto.UserCreate;
import com.docappoint.dto.UserLogin;
import com.docappoint.dto.UserResponse;
import com.docappoint.entity.Appointment;
import com.docappoint.entity.Role;
import com.docappoint.entity.Slot;
import com.docappoint.entity.Status;
import com.docappoint.entity.User;
import com.docappoint.service.AppointmentService;
import com.docappoint.service.AuthService;
import com.docappoint.service.SlotService;
import com.docappoint.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ControllersUnitTest {

    @Mock
    private AuthService authService;
    @Mock
    private AppointmentService appointmentService;
    @Mock
    private SlotService slotService;
    @Mock
    private UserService userService;

    private MockMvc authMockMvc;
    private MockMvc appointmentMockMvc;
    private MockMvc slotMockMvc;
    private MockMvc userMockMvc;

    private User testPatient;
    private User testDoctor;

    @BeforeEach
    void setUp() {
        authMockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
        appointmentMockMvc = MockMvcBuilders.standaloneSetup(new AppointmentController(appointmentService, authService)).build();
        slotMockMvc = MockMvcBuilders.standaloneSetup(new SlotController(slotService, authService)).build();
        userMockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, authService)).build();

        testPatient = new User(1, "alice", "1234567890", "hash", Role.patient);
        testDoctor = new User(2, "dr_bob", "9876543210", "hash", Role.doctor);
    }

    @Test
    void testRegisterPatient() throws Exception {
        when(authService.registerPatient(any(UserCreate.class)))
                .thenReturn(new UserResponse(1, "alice"));

        authMockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"phone\":\"1234567890\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void testRegisterDoctor() throws Exception {
        when(authService.registerDoctor(any(UserCreate.class), eq("valid_secret")))
                .thenReturn(new UserResponse(2, "dr_bob"));

        authMockMvc.perform(post("/register/doctor")
                        .param("secret", "valid_secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dr_bob\",\"phone\":\"9876543210\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("dr_bob"));
    }

    @Test
    void testLogin() throws Exception {
        when(authService.login(any(UserLogin.class)))
                .thenReturn(new TokenResponse("acc_token", "ref_token", "bearer", Role.patient));

        authMockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("acc_token"))
                .andExpect(jsonPath("$.refresh_token").value("ref_token"))
                .andExpect(jsonPath("$.token_type").value("bearer"))
                .andExpect(jsonPath("$.role").value("patient"));
    }

    @Test
    void testRefresh() throws Exception {
        when(authService.refreshToken(any(RefreshRequest.class)))
                .thenReturn(new TokenResponse("new_acc_token", "bearer"));

        authMockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refresh_token\":\"ref_token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new_acc_token"))
                .andExpect(jsonPath("$.token_type").value("bearer"));
    }

    @Test
    void testGetAppointmentsMe() throws Exception {
        when(authService.getUserFromToken("Bearer my_token")).thenReturn(testPatient);

        Slot slot = new Slot(10, testDoctor, LocalDate.of(2026, 9, 15), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.booked);
        Appointment appointment = new Appointment(100, slot, testPatient, Status.booked, null);
        when(appointmentService.getPatientAppointments(testPatient)).thenReturn(List.of(appointment));

        appointmentMockMvc.perform(get("/appointments/me")
                        .header("Authorization", "Bearer my_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appointment_id").value(100))
                .andExpect(jsonPath("$[0].date").value("2026-09-15"))
                .andExpect(jsonPath("$[0].start_time").value("09:00:00"))
                .andExpect(jsonPath("$[0].end_time").value("09:30:00"))
                .andExpect(jsonPath("$[0].status").value("booked"))
                .andExpect(jsonPath("$[0].patient_name").value("alice"));
    }

    @Test
    void testCancelAppointmentPut() throws Exception {
        when(authService.getUserFromToken("Bearer my_token")).thenReturn(testPatient);
        Slot slot = new Slot(10, testDoctor, LocalDate.of(2026, 9, 15), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.available);
        Appointment appointment = new Appointment(100, slot, testPatient, Status.cancelled, null);
        when(appointmentService.cancelAppointment(testPatient, 100)).thenReturn(appointment);

        appointmentMockMvc.perform(put("/appointments/100/cancel")
                        .header("Authorization", "Bearer my_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("cancelled"));
    }

    @Test
    void testGetSlotsAvailable() throws Exception {
        Slot slot = new Slot(10, testDoctor, LocalDate.of(2026, 9, 15), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.available);
        when(slotService.getAvailableSlots(LocalDate.of(2026, 9, 15))).thenReturn(List.of(slot));

        slotMockMvc.perform(get("/slots/available")
                        .param("date", "2026-09-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("available"));
    }

    @Test
    void testToggleFreezeSlotPatch() throws Exception {
        when(authService.getUserFromToken("Bearer doc_token")).thenReturn(testDoctor);
        Slot slot = new Slot(10, testDoctor, LocalDate.of(2026, 9, 15), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.frozen);
        when(slotService.toggleFreezeSlot(testDoctor, 10)).thenReturn(slot);

        slotMockMvc.perform(patch("/slots/10/freeze")
                        .header("Authorization", "Bearer doc_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("frozen"));
    }

    @Test
    void testGetCurrentUser() throws Exception {
        when(authService.getUserFromToken("Bearer my_token")).thenReturn(testPatient);
        when(userService.getCurrentUserDetails(testPatient)).thenReturn(new UserResponse(1, "alice"));

        userMockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer my_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"));
    }
}
