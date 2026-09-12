package com.docappoint.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "DATABASE_URL=jdbc:postgresql://localhost:5432/docappoint_test",
    "DATABASE_USERNAME=test",
    "DATABASE_PASSWORD=test",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.datasource.hikari.initialization-fail-timeout=-1"
})
class SecurityAuthorizationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void testRegisterPermittedWithoutToken() throws Exception {
        // Without Authorization header, /register must not return 401 or 403
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"phone\":\"1234567890\",\"password\":\"password123\"}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status, "/register should not return 401");
                    org.junit.jupiter.api.Assertions.assertNotEquals(403, status, "/register should not return 403");
                });
    }

    @Test
    void testLoginPermittedWithoutToken() throws Exception {
        // Without Authorization header, /login must not return 401 or 403
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"password123\"}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status, "/login should not return 401");
                    org.junit.jupiter.api.Assertions.assertNotEquals(403, status, "/login should not return 403");
                });
    }

    @Test
    void testRegisterDoctorPermittedWithoutToken() throws Exception {
        // Without Authorization header, /register/doctor must not return 401 or 403
        mockMvc.perform(post("/register/doctor")
                        .param("secret", "doc_secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dr_bob\",\"phone\":\"9876543210\",\"password\":\"password123\"}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status, "/register/doctor should not return 401");
                    org.junit.jupiter.api.Assertions.assertNotEquals(403, status, "/register/doctor should not return 403");
                });
    }

    @Test
    void testRefreshTokenPermittedWithoutToken() throws Exception {
        // Without Authorization header, /auth/refresh must not return 401 or 403
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refresh_token\":\"some_refresh_token\"}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status, "/auth/refresh should not return 401");
                    org.junit.jupiter.api.Assertions.assertNotEquals(403, status, "/auth/refresh should not return 403");
                });
    }
}
