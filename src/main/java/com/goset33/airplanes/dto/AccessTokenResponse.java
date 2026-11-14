package com.goset33.airplanes.dto;

// Ответ POST /api/auth/refresh. expiresIn — секунды до истечения access-токена
public record AccessTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
