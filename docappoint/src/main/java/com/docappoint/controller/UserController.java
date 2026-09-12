package com.docappoint.controller;

import com.docappoint.dto.UserResponse;
import com.docappoint.entity.User;
import com.docappoint.service.AuthService;
import com.docappoint.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        User currentUser = getAuthenticatedUser(authHeader);
        return ResponseEntity.ok(userService.getCurrentUserDetails(currentUser));
    }

    private User getAuthenticatedUser(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new SecurityException("Missing Authorization header");
        }
        return authService.getUserFromToken(authHeader);
    }
}
