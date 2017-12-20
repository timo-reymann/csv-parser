package com.github.timo_reymann.csv_parser.demo;

import com.github.timo_reymann.csv_parser.meta.CsvColumn;
import lombok.Data;

/**
 * @author Timrey
 * @since 20.12.17
 */
@Data
class MyBean {
    @CsvColumn(index = 0)
    private Integer id;

    @CsvColumn(index = 1)
    private String firstName;

    @CsvColumn(index = 2)
    private String lastName;

    @CsvColumn(index = 3)
    private String email;

    @CsvColumn(index = 4)
    private String gender;

    @CsvColumn(index = 5)
    private String ip;
}
