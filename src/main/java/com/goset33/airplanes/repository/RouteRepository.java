package com.goset33.airplanes.repository;

import com.goset33.airplanes.model.Route;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RouteRepository {

    private volatile Map<String, Route> routesByCode = Map.of();

    public void replaceAll(Collection<Route> routes) {
        Map<String, Route> map = new LinkedHashMap<>();
        for (Route route : routes) {
            map.put(route.code(), route);
        }
        this.routesByCode = Map.copyOf(map);
    }

    public Optional<Route> findByCode(String code) {
        return Optional.ofNullable(routesByCode.get(code));
    }

    public List<Route> findAll() {
        return List.copyOf(routesByCode.values());
    }

    public List<String> findAllCodes() {
        return List.copyOf(routesByCode.keySet());
    }
}
