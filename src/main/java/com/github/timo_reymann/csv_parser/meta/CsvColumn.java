package com.github.timo_reymann.csv_parser.meta;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Map csv file column to property
 *
 * @author Timo Reymann
 * @since 20.12.17
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvColumn {
    /**
     * Index of csv column
     *
     * @return Index of csv column
     */
    int index() default -1;

    /**
     * Header name, used if the csv should have an header or should be read by header name
     *
     * @return Header name
     */
    String headerName() default "";
}
