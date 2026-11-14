package com.goset33.airplanes.service;

import com.goset33.airplanes.client.YandexOAuthClient;
import com.goset33.airplanes.config.AppProperties;
import com.goset33.airplanes.exception.OAuthFlowException;
import com.goset33.airplanes.security.JwtService;
import com.goset33.airplanes.security.TokenType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class YandexAuthService {
    public record Started(URI authorizationUri, String stateToken) {
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private final YandexOAuthClient yandexClient;
    private final JwtService jwtService;
    private final AppProperties.Yandex config;

    public YandexAuthService(YandexOAuthClient yandexClient, JwtService jwtService, AppProperties properties) {
        this.yandexClient = yandexClient;
        this.jwtService = jwtService;
        this.config = properties.yandex();
    }

    public Started start() {
        String state = randomUrlSafe(32);
        String codeVerifier = randomUrlSafe(32);
        String codeChallenge = sha256Base64Url(codeVerifier);

        URI uri = yandexClient.buildAuthorizationUri(state, codeChallenge);
        String stateToken = jwtService.issueOAuthStateToken(state, codeVerifier);
        return new Started(uri, stateToken);
    }

    public String complete(String code, String stateFromQuery, String stateToken) {
        Jwt stateJwt;
        try {
            stateJwt = jwtService.parse(stateToken, TokenType.OAUTH_STATE);
        } catch (JwtException e) {
            throw new OAuthFlowException("invalid_state", "Token is wrong or expired", e);
        }

        String expectedState = stateJwt.getClaimAsString(JwtService.CLAIM_STATE);
        String codeVerifier = stateJwt.getClaimAsString(JwtService.CLAIM_CODE_VERIFIER);
        if (expectedState == null || codeVerifier == null || !MessageDigest.isEqual(
                expectedState.getBytes(StandardCharsets.UTF_8), stateFromQuery.getBytes(StandardCharsets.UTF_8))) {
            throw new OAuthFlowException("invalid_state", "state is wrong");
        }

        YandexOAuthClient.TokenResponse token = yandexClient.exchangeCode(code, codeVerifier);
        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
            throw new OAuthFlowException("yandex_error", "Yandex didn't return an access_token");
        }

        YandexOAuthClient.UserInfo user = yandexClient.fetchUserInfo(token.accessToken());
        if (user == null || user.id() == null || user.id().isBlank()) {
            throw new OAuthFlowException("yandex_error", "Yandex didn't return a user id");
        }

        if (!config.clientId().equals(user.clientId())) {
            throw new OAuthFlowException("yandex_error", "Token had given to an other app");
        }
        return user.id();
    }

    private static String randomUrlSafe(int bytes) {
        byte[] buffer = new byte[bytes];
        RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }

    private static String sha256Base64Url(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
