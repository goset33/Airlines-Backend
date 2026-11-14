package com.goset33.airplanes.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

// Настройки приложения (префикс app)
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Yandex yandex,
        @Valid @NotNull Auth auth
) {

    public record Jwt(
            // HS256
            @NotBlank @Size(min = 32, message = "app.jwt.secret must be at least 32 symbols") String secret,
            @NotBlank String issuer,
            @NotBlank String audience,
            @DefaultValue("15m") Duration accessTtl,
            @DefaultValue("14d") Duration refreshTtl
    ) {
    }

    public record Yandex(
            @NotBlank String clientId,
            @NotBlank String clientSecret,
            // Должен точно совпадать с Redirect URI в настройках приложения на oauth.yandex.ru
            @NotBlank String redirectUri,
            @DefaultValue("https://oauth.yandex.ru/authorize") String authorizeUri,
            @DefaultValue("https://oauth.yandex.ru/token") String tokenUri,
            @DefaultValue("https://login.yandex.ru/info") String userInfoUri
    ) {
    }

    public record Auth(
            @DefaultValue("http://localhost:8080/") String frontendSuccessUrl,
            @DefaultValue("http://localhost:8080/") String frontendFailureUrl,
            @DefaultValue("true") boolean cookieSecure,
            @DefaultValue("Strict") String cookieSameSite,
            @DefaultValue("http://localhost:8080") List<String> allowedOrigins
    ) {
    }
}
