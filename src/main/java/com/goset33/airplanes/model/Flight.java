package com.goset33.airplanes.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Строка прогноза (рейс = маршрут + дата вылета)
 *
 * @param routeCode код маршрута
 * @param date дата вылета
 * @param seats число мест в самолёте
 * @param baseDemands спрос при базовой цене для квантилей
 */
public record Flight(
        String routeCode,
        LocalDate date,
        int seats,
        List<Double> baseDemands
) {
    public Flight {
        baseDemands = List.copyOf(baseDemands);
    }
}
