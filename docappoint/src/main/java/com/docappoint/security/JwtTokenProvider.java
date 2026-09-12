package com.docappoint.security;

import com.docappoint.entity.User;

public interface JwtTokenProvider {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String validateRefreshTokenAndGetSubject(String refreshToken);

    String validateAccessTokenAndGetSubject(String accessToken);
}
