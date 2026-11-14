package com.goset33.airplanes.exception;

public class OAuthFlowException extends RuntimeException {

    private final String errorCode;

    public OAuthFlowException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public OAuthFlowException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
