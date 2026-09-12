package com.docappoint.security;

import com.docappoint.entity.Role;
import com.docappoint.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProviderImpl tokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProviderImpl("test-secret-key-that-is-at-least-256-bits-long-for-testing-purposes");
        testUser = new User(42, "dr_smith", "1234567890", "hashed_pwd", Role.doctor);
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        String accessToken = tokenProvider.generateAccessToken(testUser);
        assertNotNull(accessToken);
        assertFalse(accessToken.isBlank());

        String subject = tokenProvider.validateAccessTokenAndGetSubject(accessToken);
        assertEquals("dr_smith", subject);
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        String refreshToken = tokenProvider.generateRefreshToken(testUser);
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isBlank());

        String subject = tokenProvider.validateRefreshTokenAndGetSubject(refreshToken);
        assertEquals("dr_smith", subject);
    }

    @Test
    void testAccessTokenRejectedWhenValidatedAsRefreshToken() {
        String accessToken = tokenProvider.generateAccessToken(testUser);
        assertThrows(SecurityException.class, () -> {
            tokenProvider.validateRefreshTokenAndGetSubject(accessToken);
        });
    }

    @Test
    void testRefreshTokenRejectedWhenValidatedAsAccessToken() {
        String refreshToken = tokenProvider.generateRefreshToken(testUser);
        assertThrows(SecurityException.class, () -> {
            tokenProvider.validateAccessTokenAndGetSubject(refreshToken);
        });
    }

    @Test
    void testTamperedTokenThrowsSecurityException() {
        String token = tokenProvider.generateAccessToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";
        assertThrows(SecurityException.class, () -> {
            tokenProvider.validateAccessTokenAndGetSubject(tamperedToken);
        });
    }

    @Test
    void testBearerPrefixStripping() {
        String token = tokenProvider.generateAccessToken(testUser);
        String subject = tokenProvider.validateAccessTokenAndGetSubject("Bearer " + token);
        assertEquals("dr_smith", subject);
    }
}
