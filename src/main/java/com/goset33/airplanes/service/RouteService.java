package com.goset33.airplanes.service;

import com.goset33.airplanes.dto.RouteDto;
import com.goset33.airplanes.model.Route;
import com.goset33.airplanes.repository.FlightRepository;
import com.goset33.airplanes.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final FlightRepository flightRepository;

    public RouteService(RouteRepository routeRepository, FlightRepository flightRepository) {
        this.routeRepository = routeRepository;
        this.flightRepository = flightRepository;
    }

    public List<RouteDto> findAll() {
        return routeRepository.findAll().stream().map(this::toDto).toList();
    }

    private RouteDto toDto(Route route) {
        var range = flightRepository.findDateRange(route.code());
        return new RouteDto(
                route.code(), route.origin(), route.destination(),
                route.originCity(), route.destinationCity(),
                route.basePrice(), route.priceSensitivity(),
                range.map(FlightRepository.DateRange::from).orElse(null),
                range.map(FlightRepository.DateRange::to).orElse(null));
    }
}
