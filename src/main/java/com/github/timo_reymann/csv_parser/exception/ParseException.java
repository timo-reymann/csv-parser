package com.github.timo_reymann.csv_parser.exception;

/**
 * @author Timo Reymann
 * @since 02.09.18
 */
public class ParseException extends RuntimeException {
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
