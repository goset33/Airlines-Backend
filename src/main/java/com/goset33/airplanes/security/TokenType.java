package com.goset33.airplanes.security;

public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh"),
    OAUTH_STATE("oauth_state");

    private final String claimValue;

    TokenType(String claimValue) {
        this.claimValue = claimValue;
    }

    public String claimValue() {
        return claimValue;
    }
}
