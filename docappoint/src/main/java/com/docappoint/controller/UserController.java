package com.docappoint.controller;

import com.docappoint.dto.UserResponse;
import com.docappoint.entity.User;
import com.docappoint.service.AuthService;
import com.docappoint.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
            @AuthenticationPrincipal String username) {
        User currentUser = authService.getUserByUsername(username);
        return ResponseEntity.ok(userService.getCurrentUserDetails(currentUser));
    }
}
