package com.goset33.airplanes.data;

import com.goset33.airplanes.model.Route;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RouteCsvParser {
    private RouteCsvParser() {
    }

    public static List<Route> parse(InputStream in, String source) {
        CsvReader.Table table = CsvReader.read(in, source);
        table.requireColumns(List.of("route", "name", "base_price", "price_sensitivity"));

        List<Route> routes = new ArrayList<>();

        for (CsvReader.Row row : table.rows()) {
            String code = row.string("route").toUpperCase(Locale.ROOT);

            String name = row.string("name");
            String[] cities = name.split("\\s*—\\s*", 2);

            double basePrice = row.decimal("base_price");
            double sensitivity = row.decimal("price_sensitivity");

            routes.add(new Route(code, code.substring(0, 3), code.substring(4), cities[0], cities[1], basePrice, sensitivity));
        }

        return routes;
    }
}