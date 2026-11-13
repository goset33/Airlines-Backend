package com.goset33.airplanes.controller;

import com.goset33.airplanes.dto.ForecastRequest;
import com.goset33.airplanes.dto.ForecastResponse;
import com.goset33.airplanes.service.ForecastService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @PostMapping
    public ForecastResponse forecast(@Valid @RequestBody ForecastRequest request) {
        return forecastService.forecast(request);
    }
}
