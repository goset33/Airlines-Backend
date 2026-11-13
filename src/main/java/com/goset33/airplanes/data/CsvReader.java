package com.goset33.airplanes.data;

import com.goset33.airplanes.exception.DataLoadException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CsvReader {
    private static final CsvMapper MAPPER = CsvMapper.builder().build();
    private static final CsvSchema SCHEMA = CsvSchema.emptySchema().withHeader().withColumnSeparator(',');
    private static final TypeReference<Map<String, String>> ROW_TYPE = new TypeReference<>() {
    };

    private CsvReader() {
    }

    public record Table(String source, List<String> headers, List<Row> rows) {

        public void requireColumns(List<String> names) {
            List<String> missing = names.stream().filter(name -> !headers.contains(name)).toList();

            if (!missing.isEmpty()) {
                throw new DataLoadException("No headers: " + missing);
            }
        }

        public String findColumn(List<String> candidates) {
            for (String candidate : candidates) {
                for (String header : headers) {
                    if (header.equalsIgnoreCase(candidate)) {
                        return header;
                    }
                }
            }
            return null;
        }
    }

    public record Row(String source, int number, Map<String, String> cells) {

        public String optional(String column) {
            String value = cells.get(column);
            return value == null || value.isBlank() ? null : value.trim();
        }

        public String string(String column) {
            String value = optional(column);

            if (value == null) {
                throw error(column, "Empty value");
            }

            return value;
        }

        public double decimal(String column) {
            String value = string(column);

            return Double.parseDouble(value);
        }

        public int integer(String column) {
            String value = string(column);

            return Integer.parseInt(value);
        }

        public LocalDate date(String column) {
            String value = string(column);

            return LocalDate.parse(value);
        }

        public DataLoadException error(String column, String message) {
            return new DataLoadException(source + ", row №" + number + ", column '" + column + "': " + message);
        }
    }

    public static Table read(InputStream in, String source) {
        try (MappingIterator<Map<String, String>> iterator = MAPPER.readerFor(ROW_TYPE).with(SCHEMA).readValues(in)) {

            List<Row> rows = new ArrayList<>();
            List<String> headers = null;

            int number = 1;

            while (iterator.hasNextValue()) {
                Map<String, String> cells = iterator.nextValue();

                if (headers == null) {
                    headers = List.copyOf(cells.keySet());
                }

                rows.add(new Row(source, number++, new LinkedHashMap<>(cells)));
            }

            return new Table(source, headers, List.copyOf(rows));
        }
    }
}
