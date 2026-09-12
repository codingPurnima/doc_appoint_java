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
import com.docappoint.exception.GlobalExceptionHandler;
import com.docappoint.service.AppointmentService;
import com.docappoint.service.AuthService;
import com.docappoint.service.SlotService;
import com.docappoint.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                Principal principal = webRequest.getUserPrincipal();
                return principal != null ? principal.getName() : null;
            }
        };

        authMockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(exceptionHandler)
                .build();
        appointmentMockMvc = MockMvcBuilders.standaloneSetup(new AppointmentController(appointmentService, authService))
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(exceptionHandler)
                .build();
        slotMockMvc = MockMvcBuilders.standaloneSetup(new SlotController(slotService, authService))
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(exceptionHandler)
                .build();
        userMockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, authService))
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(exceptionHandler)
                .build();

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
    void testBookAppointment() throws Exception {
        when(authService.getUserByUsername("alice")).thenReturn(testPatient);
        Slot slot = new Slot(10, testDoctor, LocalDate.of(2026, 9, 20), LocalTime.of(10, 0), LocalTime.of(10, 30), Status.booked);
        Appointment appointment = new Appointment(101, slot, testPatient, Status.booked, null);
        when(appointmentService.bookAppointment(eq(testPatient), any(AppointmentCreate.class))).thenReturn(appointment);

        appointmentMockMvc.perform(post("/appointments/book")
                        .principal(new TestingAuthenticationToken("alice", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slot_id\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.status").value("booked"));
    }

    @Test
    void testCompleteAppointment() throws Exception {
        when(authService.getUserByUsername("dr_bob")).thenReturn(testDoctor);
        Slot slot = new Slot(10, testDoctor, LocalDate.of(2026, 9, 10), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.completed);
        Appointment appointment = new Appointment(100, slot, testPatient, Status.completed, null);
        when(appointmentService.completeAppointment(testDoctor, 100)).thenReturn(appointment);

        appointmentMockMvc.perform(patch("/appointments/100/complete")
                        .principal(new TestingAuthenticationToken("dr_bob", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("completed"));
    }

    @Test
    void testGetAppointmentsMe() throws Exception {
        when(authService.getUserByUsername("alice")).thenReturn(testPatient);

        Slot slot = new Slot(10, testDoctor, LocalDate.of(2026, 9, 15), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.booked);
        Appointment appointment = new Appointment(100, slot, testPatient, Status.booked, null);
        when(appointmentService.getPatientAppointments(testPatient)).thenReturn(List.of(appointment));

        appointmentMockMvc.perform(get("/appointments/me")
                        .principal(new TestingAuthenticationToken("alice", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appointment_id").value(100))
                .andExpect(jsonPath("$[0].date").value("2026-09-15"))
                .andExpect(jsonPath("$[0].start_time").value("09:00:00"))
                .andExpect(jsonPath("$[0].end_time").value("09:30:00"))
                .andExpect(jsonPath("$[0].status").value("booked"))
                .andExpect(jsonPath("$[0].patient_name").value("alice"));
    }

    @Test
    void testGetDoctorAppointments() throws Exception {
        when(authService.getUserByUsername("dr_bob")).thenReturn(testDoctor);
        DoctorAppointmentResponse response = new DoctorAppointmentResponse(
                100, LocalDate.of(2026, 9, 15), LocalTime.of(9, 0), LocalTime.of(9, 30), "booked", "alice"
        );
        when(appointmentService.getDoctorAppointments(testDoctor)).thenReturn(List.of(response));

        appointmentMockMvc.perform(get("/appointments/doctor")
                        .principal(new TestingAuthenticationToken("dr_bob", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appointment_id").value(100))
                .andExpect(jsonPath("$[0].patient_name").value("alice"));
    }

    @Test
    void testCancelAppointmentPut() throws Exception {
        when(authService.getUserByUsername("alice")).thenReturn(testPatient);
        Slot slot = new Slot(10, testDoctor, LocalDate.of(2026, 9, 25), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.available);
        Appointment appointment = new Appointment(100, slot, testPatient, Status.cancelled, null);
        when(appointmentService.cancelAppointment(testPatient, 100)).thenReturn(appointment);

        appointmentMockMvc.perform(put("/appointments/100/cancel")
                        .principal(new TestingAuthenticationToken("alice", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("cancelled"));
    }

    @Test
    void testGenerateSlots() throws Exception {
        when(authService.getUserByUsername("dr_bob")).thenReturn(testDoctor);
        Slot slot = new Slot(1, testDoctor, LocalDate.of(2026, 9, 20), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.available);
        when(slotService.generateSlots(eq(testDoctor), any(SlotGenerateRequest.class))).thenReturn(List.of(slot));

        slotMockMvc.perform(post("/slots/generate")
                        .principal(new TestingAuthenticationToken("dr_bob", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-09-20\",\"day_start\":\"09:00:00\",\"day_end\":\"17:00:00\",\"slot_duration_minutes\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("available"));
    }

    @Test
    void testGetDoctorSlots() throws Exception {
        when(authService.getUserByUsername("dr_bob")).thenReturn(testDoctor);
        Slot slot = new Slot(1, testDoctor, LocalDate.of(2026, 9, 20), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.available);
        when(slotService.getDoctorSlots(testDoctor, LocalDate.of(2026, 9, 20))).thenReturn(List.of(slot));

        slotMockMvc.perform(get("/slots")
                        .principal(new TestingAuthenticationToken("dr_bob", null))
                        .param("date", "2026-09-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
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
        when(authService.getUserByUsername("dr_bob")).thenReturn(testDoctor);
        Slot slot = new Slot(10, testDoctor, LocalDate.of(2026, 9, 15), LocalTime.of(9, 0), LocalTime.of(9, 30), Status.frozen);
        when(slotService.toggleFreezeSlot(testDoctor, 10)).thenReturn(slot);

        slotMockMvc.perform(patch("/slots/10/freeze")
                        .principal(new TestingAuthenticationToken("dr_bob", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("frozen"));
    }

    @Test
    void testGetCurrentUser() throws Exception {
        when(authService.getUserByUsername("alice")).thenReturn(testPatient);
        when(userService.getCurrentUserDetails(testPatient)).thenReturn(new UserResponse(1, "alice"));

        userMockMvc.perform(get("/users/me")
                        .principal(new TestingAuthenticationToken("alice", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void testConflictHandlingReturns409() throws Exception {
        when(authService.getUserByUsername("alice")).thenReturn(testPatient);
        when(appointmentService.bookAppointment(eq(testPatient), any(AppointmentCreate.class)))
                .thenThrow(new IllegalStateException("Slot is not available for booking"));

        appointmentMockMvc.perform(post("/appointments/book")
                        .principal(new TestingAuthenticationToken("alice", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slot_id\":10}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Slot is not available for booking"));
    }

    @Test
    void testBadRequestHandlingReturns400() throws Exception {
        when(authService.getUserByUsername("dr_bob")).thenReturn(testDoctor);
        when(slotService.generateSlots(eq(testDoctor), any(SlotGenerateRequest.class)))
                .thenThrow(new IllegalArgumentException("day_start must be before day_end"));

        slotMockMvc.perform(post("/slots/generate")
                        .principal(new TestingAuthenticationToken("dr_bob", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2026-09-20\",\"day_start\":\"17:00:00\",\"day_end\":\"09:00:00\",\"slot_duration_minutes\":30}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("day_start must be before day_end"));
    }

    @Test
    void testForbiddenHandlingReturns403() throws Exception {
        when(authService.getUserByUsername("alice")).thenReturn(testPatient);
        when(appointmentService.completeAppointment(testPatient, 100))
                .thenThrow(new SecurityException("Only doctors can mark appointments as completed"));

        appointmentMockMvc.perform(patch("/appointments/100/complete")
                        .principal(new TestingAuthenticationToken("alice", null)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Only doctors can mark appointments as completed"));
    }
}
