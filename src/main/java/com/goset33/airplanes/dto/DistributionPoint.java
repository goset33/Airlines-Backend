package com.goset33.airplanes.dto;

public record DistributionPoint(
        double quantile,
        double baseDemand,
        double demand,
        double sales
) {
}
