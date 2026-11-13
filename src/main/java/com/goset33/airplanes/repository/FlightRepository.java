package com.goset33.airplanes.repository;

import com.goset33.airplanes.model.Flight;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public class FlightRepository {

    public record DateRange(LocalDate from, LocalDate to) {
    }

    private volatile Map<String, TreeMap<LocalDate, Flight>> flightsByRoute = Map.of();

    public void replaceAll(Collection<Flight> flights) {
        Map<String, TreeMap<LocalDate, Flight>> map = new HashMap<>();
        for (Flight flight : flights) {
            map.computeIfAbsent(flight.routeCode(), k -> new TreeMap<>()).put(flight.date(), flight);
        }
        this.flightsByRoute = map;
    }

    public Optional<Flight> find(String routeCode, LocalDate date) {
        TreeMap<LocalDate, Flight> byDate = flightsByRoute.get(routeCode);
        return byDate == null ? Optional.empty() : Optional.ofNullable(byDate.get(date));
    }

    public Optional<DateRange> findDateRange(String routeCode) {
        TreeMap<LocalDate, Flight> byDate = flightsByRoute.get(routeCode);
        if (byDate == null || byDate.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DateRange(byDate.firstKey(), byDate.lastKey()));
    }

    public int count() {
        return flightsByRoute.values().stream().mapToInt(Map::size).sum();
    }
}
