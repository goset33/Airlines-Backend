package com.goset33.airplanes.model;

/**
 * Маршрут из routes.csv
 *
 * @param code код маршрута (MOW-AER)
 * @param origin код пункта отправления (MOW)
 * @param destination код пункта назначения (AER)
 * @param originCity название города отправления
 * @param destinationCity название города назначения (может быть null)
 * @param basePrice базовая цена, при которой заданы base_demand в прогнозе
 * @param priceSensitivity на сколько падает спрос при росте цены на 1 денежную единицу
 */
public record Route(
        String code,
        String origin,
        String destination,
        String originCity,
        String destinationCity,
        double basePrice,
        double priceSensitivity
) {
}
