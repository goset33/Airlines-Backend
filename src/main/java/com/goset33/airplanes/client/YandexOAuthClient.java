package com.goset33.airplanes.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.goset33.airplanes.config.AppProperties;
import com.goset33.airplanes.exception.OAuthFlowException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class YandexOAuthClient {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Long expiresIn
    ) {
    }

    // id -- уникальный идентификатор пользователя, client_id -- приложение, которому выдан токен
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserInfo(
            String id,
            String login,
            @JsonProperty("client_id") String clientId,
            String psuid
    ) {
    }

    private final AppProperties.Yandex config;
    private final RestClient restClient;

    public YandexOAuthClient(AppProperties properties) {
        this.config = properties.yandex();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public URI buildAuthorizationUri(String state, String codeChallenge) {
        return UriComponentsBuilder.fromUriString(config.authorizeUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", config.clientId())
                .queryParam("redirect_uri", config.redirectUri())
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build().encode().toUri();
    }

    // Обмен authorization code на OAuth access token
    public TokenResponse exchangeCode(String code, String codeVerifier) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("code_verifier", codeVerifier);

        try {
            return restClient.post()
                    .uri(config.tokenUri())
                    .headers(h -> h.setBasicAuth(config.clientId(), config.clientSecret()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);
        } catch (RestClientResponseException e) {
            throw new OAuthFlowException("yandex_error",
                    "Yandex returned an error, HTTP " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new OAuthFlowException("yandex_error", "Can't reach " + config.tokenUri(), e);
        }
    }

    public UserInfo fetchUserInfo(String oauthAccessToken) {
        URI uri = UriComponentsBuilder.fromUriString(config.userInfoUri())
                .queryParam("format", "json")
                .build()
                .toUri();
        try {
            return restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "OAuth " + oauthAccessToken)
                    .retrieve()
                    .body(UserInfo.class);
        } catch (RestClientResponseException e) {
            throw new OAuthFlowException("yandex_error",
                    "Yandex did't return any data, HTTP " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new OAuthFlowException("yandex_error", "Can't reach " + config.userInfoUri(), e);
        }
    }
}
