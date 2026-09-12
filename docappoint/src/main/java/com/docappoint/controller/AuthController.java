package com.docappoint.controller;

import com.docappoint.dto.RefreshRequest;
import com.docappoint.dto.TokenResponse;
import com.docappoint.dto.UserCreate;
import com.docappoint.dto.UserLogin;
import com.docappoint.dto.UserResponse;
import com.docappoint.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreate request) {
        return ResponseEntity.ok(authService.registerPatient(request));
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<UserResponse> registerDoctor(
            @Valid @RequestBody UserCreate request,
            @RequestParam("secret") String secret) {
        return ResponseEntity.ok(authService.registerDoctor(request, secret));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody UserLogin request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
