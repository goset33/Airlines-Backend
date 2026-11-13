package com.goset33.airplanes.data;

import com.goset33.airplanes.model.Flight;
import com.goset33.airplanes.util.Quantiles;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FlightCsvParser {

    private FlightCsvParser() {
    }

    public static List<Flight> parse(InputStream in, String source) {
        CsvReader.Table table = CsvReader.read(in, source);

        List<String> required = new ArrayList<>(List.of("route", "date", "seats"));
        for (String q : Quantiles.LABELS) {
            required.add(q + "_base_demand");
        }
        table.requireColumns(required);

        List<Flight> flights = new ArrayList<>();

        for (CsvReader.Row row : table.rows()) {
            String route = row.string("route").toUpperCase(Locale.ROOT);
            int seats = row.integer("seats");

            List<Double> baseDemands = new ArrayList<>(Quantiles.COUNT);
            for (String q : Quantiles.LABELS) {
                baseDemands.add(row.decimal(q + "_base_demand"));
            }

            flights.add(new Flight(route, row.date("date"), seats, baseDemands));
        }

        return flights;
    }
}