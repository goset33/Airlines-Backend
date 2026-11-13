package com.goset33.airplanes.data;

import com.goset33.airplanes.exception.DataLoadException;
import com.goset33.airplanes.model.Flight;
import com.goset33.airplanes.model.Route;
import com.goset33.airplanes.repository.FlightRepository;
import com.goset33.airplanes.repository.RouteRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// app.data.routes-file, app.data.forecast-file
@Component
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final RouteRepository routeRepository;
    private final FlightRepository flightRepository;
    private final String routesFile;
    private final String forecastFile;

    public DataLoader(RouteRepository routeRepository, FlightRepository flightRepository,
                      @Value("${app.data.routes-file:routes.csv}") String routesFile,
                      @Value("${app.data.forecast-file:seasonal_forecast.csv}") String forecastFile) {
        this.routeRepository = routeRepository;
        this.flightRepository = flightRepository;
        this.routesFile = routesFile;
        this.forecastFile = forecastFile;
    }

    @PostConstruct
    void load() {
        List<Route> routes = RouteCsvParser.parse(open(routesFile), routesFile);
        List<Flight> flights = FlightCsvParser.parse(open(forecastFile), forecastFile);

        routeRepository.replaceAll(routes);
        flightRepository.replaceAll(flights);
        log.info("Data loaded: flights {}, routes {} ({}, {})",
                routes.size(), flightRepository.count(), routesFile, forecastFile);
    }

    private static InputStream open(String name) {
        ClassPathResource resource = new ClassPathResource(name);
        if (!resource.exists()) {
            throw new DataLoadException("No file " + name + " in the classpath (src/main/resources)");
        }
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            throw new DataLoadException("Can't open " + name, e);
        }
    }
}
