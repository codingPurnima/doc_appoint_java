package com.docappoint.service;

import com.docappoint.dto.RefreshRequest;
import com.docappoint.dto.TokenResponse;
import com.docappoint.dto.UserCreate;
import com.docappoint.dto.UserLogin;
import com.docappoint.dto.UserResponse;
import com.docappoint.entity.Role;
import com.docappoint.entity.User;
import com.docappoint.repository.UserRepository;
import com.docappoint.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${DOCTOR_REGISTER_SECRET:${doctor.register.secret:}}")
    private String doctorRegisterSecret;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public UserResponse registerPatient(UserCreate request) {
        validateUniqueUser(request.username(), request.phone());

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.username(), request.phone(), hashedPassword, Role.patient);
        User saved = userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public UserResponse registerDoctor(UserCreate request, String doctorSecret) {
        if (doctorRegisterSecret == null || doctorRegisterSecret.isBlank() || !doctorRegisterSecret.equals(doctorSecret)) {
            throw new SecurityException("Invalid doctor registration secret");
        }

        validateUniqueUser(request.username(), request.phone());

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.username(), request.phone(), hashedPassword, Role.doctor);
        User saved = userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getName());
    }

    public TokenResponse login(UserLogin request) {
        User user = userRepository.findByName(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getHashedPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return new TokenResponse(accessToken, refreshToken, "bearer", user.getRole());
    }

    public TokenResponse refreshToken(RefreshRequest request) {
        String username = jwtTokenProvider.validateRefreshTokenAndGetSubject(request.refreshToken());
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found from refresh token"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        return new TokenResponse(newAccessToken, "bearer");
    }

    public User getUserFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String username = jwtTokenProvider.validateAccessTokenAndGetSubject(token);
        return userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found from token: " + username));
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private void validateUniqueUser(String username, String phone) {
        if (userRepository.existsByName(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone number is already registered");
        }
    }
}
