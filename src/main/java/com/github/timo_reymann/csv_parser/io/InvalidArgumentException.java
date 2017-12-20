package com.github.timo_reymann.csv_parser.io;

/**
 * Invalid argument for csv reader/writer builder
 *
 * @author Timo Reymann
 * @since 20.12.17
 */
public class InvalidArgumentException extends RuntimeException {
    /**
     * Create new instance.
     *
     * @param argument Argument name
     * @param value    Value for argument
     */
    InvalidArgumentException(String argument, Object value) {
        super("Invalid value " + value + " for argument " + argument);
    }

    /**
     * Create new instance.
     *
     * @param argument Argument
     * @param e        Exception occurred during setting argument
     */
    InvalidArgumentException(String argument, Exception e) {
        super("Invalid argument " + argument, e);
    }
}