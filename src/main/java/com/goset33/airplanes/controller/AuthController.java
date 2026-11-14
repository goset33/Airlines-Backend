package com.goset33.airplanes.controller;

import com.goset33.airplanes.config.AppProperties;
import com.goset33.airplanes.dto.AccessTokenResponse;
import com.goset33.airplanes.dto.MeResponse;
import com.goset33.airplanes.exception.OAuthFlowException;
import com.goset33.airplanes.exception.UnauthorizedException;
import com.goset33.airplanes.security.AuthCookies;
import com.goset33.airplanes.security.JwtService;
import com.goset33.airplanes.security.TokenType;
import com.goset33.airplanes.service.YandexAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final YandexAuthService yandexAuthService;
    private final JwtService jwtService;
    private final AuthCookies cookies;
    private final AppProperties.Auth authProperties;

    public AuthController(YandexAuthService yandexAuthService, JwtService jwtService, AuthCookies cookies, AppProperties properties) {
        this.yandexAuthService = yandexAuthService;
        this.jwtService = jwtService;
        this.cookies = cookies;
        this.authProperties = properties.auth();
    }

    @GetMapping("/yandex")
    public ResponseEntity<Void> startYandexLogin() {
        YandexAuthService.Started started = yandexAuthService.start();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(started.authorizationUri())
                .header(HttpHeaders.SET_COOKIE, cookies.state(started.stateToken()).toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    @GetMapping("/yandex/callback")
    public ResponseEntity<Void> yandexCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @CookieValue(name = AuthCookies.STATE_COOKIE, required = false) String stateToken) {

        ResponseCookie clearState = cookies.clearState();

        if (error != null) {
            log.info("Verification error from Yandex: {}", error);
            return redirect(failureUri("access_denied".equals(error) ? "access_denied" : "yandex_error"), clearState);
        }
        if (isBlank(code) || isBlank(state) || isBlank(stateToken)) {
            return redirect(failureUri("invalid_request"), clearState);
        }

        try {
            String userId = yandexAuthService.complete(code, state, stateToken);
            ResponseCookie refresh = cookies.refresh(jwtService.issueRefreshToken(userId));
            return redirect(URI.create(authProperties.frontendSuccessUrl()), clearState, refresh);
        } catch (OAuthFlowException e) {
            log.warn("Can't log in with Yandex [{}]: {}", e.getErrorCode(), e.getMessage());
            return redirect(failureUri(e.getErrorCode()), clearState);
        } catch (RuntimeException e) {
            log.error("Unhandled exception while logging in with Yandex", e);
            return redirect(failureUri("server_error"), clearState);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @CookieValue(name = AuthCookies.REFRESH_COOKIE, required = false) String refreshToken) {
        if (isBlank(refreshToken)) {
            throw new UnauthorizedException("No Refresh token");
        }

        Jwt jwt;
        try {
            jwt = jwtService.parse(refreshToken, TokenType.REFRESH);
        } catch (JwtException e) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        AccessTokenResponse body = new AccessTokenResponse(
                jwtService.issueAccessToken(jwt.getSubject()),
                "Bearer",
                jwtService.accessTtl().toSeconds());

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(body);
    }

    // Удаляет refresh-cookie. Это всё, что я могу сделать без БД
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.clearRefresh().toString())
                .build();
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return new MeResponse(jwt.getSubject(), true);
    }

    private URI failureUri(String errorCode) {
        return UriComponentsBuilder.fromUriString(authProperties.frontendFailureUrl())
                .queryParam("auth_error", errorCode)
                .build()
                .toUri();
    }

    private static ResponseEntity<Void> redirect(URI location, ResponseCookie... setCookies) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .header(HttpHeaders.CACHE_CONTROL, "no-store");
        for (ResponseCookie cookie : setCookies) {
            builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return builder.build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
