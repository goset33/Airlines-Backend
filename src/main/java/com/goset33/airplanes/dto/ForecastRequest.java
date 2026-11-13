package com.goset33.airplanes.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Пример: {"origin":"MOW","destination":"AER","date":"2026-06-15","price":8500}
public record ForecastRequest(

        @NotBlank(message = "No origin")
        @Pattern(regexp = "^[A-Za-z]{3}$")
        String origin,

        @NotBlank(message = "No destination")
        @Pattern(regexp = "^[A-Za-z]{3}$")
        String destination,

        @NotNull(message = "No date (yyyy-MM-dd)")
        LocalDate date,

        @NotNull(message = "No price")
        @DecimalMin(value = "0", inclusive = false, message = "Price must be greater than 0")
        @DecimalMax(value = "10000000", message = "Price must be less than 10 000 000")
        BigDecimal price
) {
}
