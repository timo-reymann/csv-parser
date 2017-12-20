package com.github.timo_reymann.csv_parser.exception;

/**
 * Error during mapping
 *
 * @author Timo Reymann
 * @since 20.12.17
 */
public class MappingException extends RuntimeException {
    /**
     * Create mapping exception
     *
     * @param msg Message to display for the user
     */
    public MappingException(String msg) {
        super(msg);
    }
}
