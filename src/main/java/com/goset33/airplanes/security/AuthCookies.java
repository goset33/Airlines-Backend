package com.goset33.airplanes.security;

import com.goset33.airplanes.config.AppProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

// Фабрика cookies
@Component
public class AuthCookies {

    public static final String REFRESH_COOKIE = "refresh_token";
    public static final String STATE_COOKIE = "yandex_oauth_state";

    private static final String REFRESH_PATH = "/api/auth/refresh";
    private static final String STATE_PATH = "/api/auth/yandex/callback";
    private static final Duration STATE_MAX_AGE = Duration.ofMinutes(10);

    private final AppProperties.Auth auth;
    private final Duration refreshTtl;

    public AuthCookies(AppProperties properties) {
        this.auth = properties.auth();
        this.refreshTtl = properties.jwt().refreshTtl();
    }

    public ResponseCookie refresh(String refreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(auth.cookieSecure() || "None".equalsIgnoreCase(auth.cookieSameSite()))
                .sameSite(auth.cookieSameSite())
                .path(REFRESH_PATH)
                .maxAge(refreshTtl)
                .build();
    }

    public ResponseCookie clearRefresh() {
        return ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(auth.cookieSecure() || "None".equalsIgnoreCase(auth.cookieSameSite()))
                .sameSite(auth.cookieSameSite())
                .path(REFRESH_PATH)
                .maxAge(Duration.ZERO)
                .build();
    }

    public ResponseCookie state(String stateToken) {
        return ResponseCookie.from(STATE_COOKIE, stateToken)
                .httpOnly(true)
                .secure(auth.cookieSecure())
                .sameSite("Lax")
                .path(STATE_PATH)
                .maxAge(STATE_MAX_AGE)
                .build();
    }

    public ResponseCookie clearState() {
        return ResponseCookie.from(STATE_COOKIE, "")
                .httpOnly(true)
                .secure(auth.cookieSecure())
                .sameSite("Lax")
                .path(STATE_PATH)
                .maxAge(Duration.ZERO)
                .build();
    }
}
