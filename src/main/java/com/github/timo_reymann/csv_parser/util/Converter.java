package com.github.timo_reymann.csv_parser.util;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;

public final class Converter {
    private static final HashMap<String, DateTimeFormatter> FORMATTER_CACHE = new HashMap<>();

    public int convertToInt(String input) {
        return Integer.parseInt(input);
    }

    public float convertToFloat(String input) {
        return Float.parseFloat(input);
    }

    public boolean convertToBoolean(String input) {
        return Boolean.parseBoolean(input);
    }

    public double convertToDouble(String input) {
        return Double.parseDouble(input);
    }

    public LocalDateTime convertToLocalDateTime(String pattern, String input) {
        return LocalDateTime.parse(input, getFormatter(pattern));
    }

    public LocalDate convertToLocalDate(String pattern, String input) {
        return LocalDate.parse(input, getFormatter(pattern));
    }

    private DateTimeFormatter getFormatter(String pattern) {
        DateTimeFormatter formatter = FORMATTER_CACHE.get(pattern);

        if (formatter == null) {
            formatter = DateTimeFormatter.ofPattern(pattern);
            FORMATTER_CACHE.put(pattern, formatter);
        }

        return formatter;
    }

    public <T> void setField(Field field, Object object, T value) throws IllegalAccessException {
        field.set(object, value);
    }

    public String formatLocalDateTime(String pattern, LocalDateTime value) {
        return format(pattern, value);
    }

    private String format(String pattern, TemporalAccessor value) {
        if (value == null)
            return "";

        return getFormatter(pattern).format(value);
    }

    public String formatLocalDate(String pattern, LocalDate value) {
        return format(pattern, value);
    }
}
