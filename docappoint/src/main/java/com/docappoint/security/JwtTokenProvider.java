package com.docappoint.security;

import com.docappoint.entity.User;
import io.jsonwebtoken.Claims;

public interface JwtTokenProvider {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String validateRefreshTokenAndGetSubject(String refreshToken);

    String validateAccessTokenAndGetSubject(String accessToken);

    Claims parseAccessToken(String accessToken);
}
