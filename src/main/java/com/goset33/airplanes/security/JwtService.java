package com.goset33.airplanes.security;

import com.goset33.airplanes.config.AppProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class JwtService {

    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String CLAIM_STATE = "state";
    public static final String CLAIM_CODE_VERIFIER = "cv";

    private static final Duration OAUTH_STATE_TTL = Duration.ofMinutes(10);
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(5);

    private final AppProperties.Jwt config;
    private final JwtEncoder encoder;
    private final Map<TokenType, JwtDecoder> decoders = new EnumMap<>(TokenType.class);

    public JwtService(AppProperties properties) {
        this.config = properties.jwt();

        byte[] secret = config.secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 symbols");
        }
        SecretKey key = new SecretKeySpec(secret, "HmacSHA256");

        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        for (TokenType type : TokenType.values()) {
            decoders.put(type, buildDecoder(key, type));
        }
    }

    public String issueAccessToken(String userId) {
        return issue(TokenType.ACCESS, userId, config.accessTtl(), Map.of());
    }

    public String issueRefreshToken(String userId) {
        return issue(TokenType.REFRESH, userId, config.refreshTtl(), Map.of());
    }

    public String issueOAuthStateToken(String state, String codeVerifier) {
        return issue(TokenType.OAUTH_STATE, "oauth-state", OAUTH_STATE_TTL,
                Map.of(CLAIM_STATE, state, CLAIM_CODE_VERIFIER, codeVerifier));
    }

    public Jwt parse(String token, TokenType expectedType) {
        return decoders.get(expectedType).decode(token);
    }

    public JwtDecoder accessDecoder() {
        return decoders.get(TokenType.ACCESS);
    }

    public Duration accessTtl() {
        return config.accessTtl();
    }

    private String issue(TokenType type, String subject, Duration ttl, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(config.issuer())
                .audience(List.of(config.audience()))
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TOKEN_TYPE, type.claimValue());
        extraClaims.forEach(claims::claim);

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    private JwtDecoder buildDecoder(SecretKey key, TokenType type) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(CLOCK_SKEW),
                new JwtIssuerValidator(config.issuer()),
                new JwtClaimValidator<Collection<String>>(JwtClaimNames.AUD,
                        aud -> aud != null && aud.contains(config.audience())),
                new JwtClaimValidator<String>(JwtClaimNames.SUB, sub -> sub != null && !sub.isBlank()),
                new JwtClaimValidator<Instant>(JwtClaimNames.EXP, Objects::nonNull),
                new JwtClaimValidator<Instant>(JwtClaimNames.IAT, Objects::nonNull),
                new JwtClaimValidator<String>(CLAIM_TOKEN_TYPE, type.claimValue()::equals));
        decoder.setJwtValidator(validator);
        return decoder;
    }
}
