package com.goset33.airplanes.util;

import java.util.List;

public final class Quantiles {

    public static final List<String> LABELS = List.of("0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9");
    public static final List<Double> VALUES = LABELS.stream().map(Double::parseDouble).toList();
    public static final int COUNT = LABELS.size();
    public static final int MEDIAN_INDEX = LABELS.indexOf("0.5");

    private Quantiles() {
    }
}
