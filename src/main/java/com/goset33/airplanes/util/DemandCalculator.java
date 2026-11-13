package com.goset33.airplanes.util;

public final class DemandCalculator {

    private DemandCalculator() {
    }

    // Спрос при заданной цене
    public static double demand(double baseDemand, double priceSensitivity, double price, double basePrice) {
        return baseDemand - priceSensitivity * (price - basePrice);
    }

    public static double sales(double demand, int seats) {
        return Math.min(seats, Math.max(0.0, demand));
    }

    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
