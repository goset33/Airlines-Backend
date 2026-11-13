package com.goset33.airplanes.dto;

import java.time.LocalDate;

public record RouteDto(
        String code,
        String origin,
        String destination,
        String originCity,
        String destinationCity,
        double basePrice,
        double priceSensitivity,
        LocalDate firstDate,
        LocalDate lastDate
) {
}
