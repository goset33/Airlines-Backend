package com.goset33.airplanes.dto;

import java.time.LocalDate;
import java.util.List;

// Данные для графика
public record DistributionResponse(
        String route,
        String origin,
        String destination,
        String originCity,
        String destinationCity,
        LocalDate date,
        double price,
        double basePrice,
        double priceSensitivity,
        int seats,
        List<DistributionPoint> points
) {
}
