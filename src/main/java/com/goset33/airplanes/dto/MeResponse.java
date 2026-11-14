package com.goset33.airplanes.dto;

// Ответ GET /api/auth/me
public record MeResponse(
        String userId,
        boolean authenticated
) {
}
