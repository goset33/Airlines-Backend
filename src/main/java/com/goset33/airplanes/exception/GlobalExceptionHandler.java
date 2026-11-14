package com.goset33.airplanes.exception;

import com.goset33.airplanes.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Некорректные входные данные", fieldErrors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Тело запроса отсутствует или некорректно: ожидается JSON, date — yyyy-MM-dd, price — число",
                null, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Некорректное значение параметра '" + e.getName() + "'", null, request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidRequestException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), null, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), null, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Ресурс не найден", null, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Метод " + e.getMethod() + " не поддерживается", null, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Ожидается Content-Type: application/json", null, request);
    }

    @ExceptionHandler({AuthenticationException.class, UnauthorizedException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException e, HttpServletRequest request) {
        String message = e instanceof UnauthorizedException ? e.getMessage()
                : "Требуется авторизация: токен отсутствует, недействителен или просрочен";
        return build(HttpStatus.UNAUTHORIZED, message, null, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException e, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Доступ запрещён", null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("Необработанная ошибка при обработке {} {}", request.getMethod(), request.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", null, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, Map<String, String> fieldErrors, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message, fieldErrors,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
