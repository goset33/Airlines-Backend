package com.goset33.airplanes.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Простой прогноз по медиане
 *
 * @param demand                необрезанный спрос
 * @param predictedSales        прогноз продаж
 * @param predictedSalesRounded то же, округлённое до целых билетов
 * @param freeSeats             свободные места
 * @param capacityReached       true, если спрос упирается во вместимость
 * @param points                9 квантилей
 */
public record ForecastResponse(
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
        double demand,
        double predictedSales,
        int predictedSalesRounded,
        int freeSeats,
        boolean capacityReached,
        List<DistributionPoint> points
) {
}
