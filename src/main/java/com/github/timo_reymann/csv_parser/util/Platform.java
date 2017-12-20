package com.github.timo_reymann.csv_parser.util;

/**
 * Platform specific functionality
 *
 * @author Timo Reymann
 * @since 20.12.17
 */
public class Platform {
    /**
     * Get new line control codes for platform
     *
     * @return Control code for new line
     */
    public static String getLineSeperator() {
        return System.getProperty("line.separator");
    }
}
