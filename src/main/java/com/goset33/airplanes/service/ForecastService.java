package com.goset33.airplanes.service;

import com.goset33.airplanes.dto.DistributionPoint;
import com.goset33.airplanes.dto.ForecastRequest;
import com.goset33.airplanes.dto.ForecastResponse;
import com.goset33.airplanes.exception.InvalidRequestException;
import com.goset33.airplanes.exception.NotFoundException;
import com.goset33.airplanes.model.Flight;
import com.goset33.airplanes.model.Route;
import com.goset33.airplanes.repository.FlightRepository;
import com.goset33.airplanes.repository.RouteRepository;
import com.goset33.airplanes.util.DemandCalculator;
import com.goset33.airplanes.util.Quantiles;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.goset33.airplanes.util.DemandCalculator.round2;

@Service
public class ForecastService {

    private final RouteRepository routeRepository;
    private final FlightRepository flightRepository;

    public ForecastService(RouteRepository routeRepository, FlightRepository flightRepository) {
        this.routeRepository = routeRepository;
        this.flightRepository = flightRepository;
    }

    public ForecastResponse forecast(ForecastRequest request) {
        Resolved r = resolve(request);
        int seats = r.flight.seats();

        List<DistributionPoint> points = new ArrayList<>(Quantiles.COUNT);
        for (int i = 0; i < Quantiles.COUNT; i++) {
            double baseDemand = r.flight.baseDemands().get(i);
            double demand = DemandCalculator.demand(baseDemand, r.route.priceSensitivity(), r.price, r.route.basePrice());
            double sales = DemandCalculator.sales(demand, seats);
            points.add(new DistributionPoint(Quantiles.VALUES.get(i), round2(baseDemand), round2(demand), round2(sales)));
        }

        double medianDemand = DemandCalculator.demand(
                r.flight.baseDemands().get(Quantiles.MEDIAN_INDEX),
                r.route.priceSensitivity(), r.price, r.route.basePrice());
        double medianSales = DemandCalculator.sales(medianDemand, seats);
        int salesRounded = (int) Math.round(medianSales);

        return new ForecastResponse(
                r.route.code(), r.route.origin(), r.route.destination(),
                r.route.originCity(), r.route.destinationCity(),
                r.flight.date(), r.price, r.route.basePrice(), r.route.priceSensitivity(),
                seats,
                round2(medianDemand),
                round2(medianSales),
                salesRounded,
                seats - salesRounded,
                medianDemand >= seats,
                points);
    }

    private record Resolved(Route route, Flight flight, double price) {
    }

    private Resolved resolve(ForecastRequest request) {
        String origin = request.origin().trim().toUpperCase(Locale.ROOT);
        String destination = request.destination().trim().toUpperCase(Locale.ROOT);
        if (origin.equals(destination)) {
            throw new InvalidRequestException("Same origin and destination: " + origin);
        }

        String code = origin + "-" + destination;
        Route route = routeRepository.findByCode(code).orElseThrow(() -> new NotFoundException("Route " + code + " not found"));

        Flight flight = flightRepository.find(code, request.date()).orElseThrow(() -> {
            String range = flightRepository.findDateRange(code)
                    .map(d -> "Have forecast for dates from " + d.from() + " to " + d.to()).orElse("");
            return new NotFoundException("Route " + code + " for " + request.date() + " not found. " + range);
        });

        return new Resolved(route, flight, request.price().doubleValue());
    }
}
