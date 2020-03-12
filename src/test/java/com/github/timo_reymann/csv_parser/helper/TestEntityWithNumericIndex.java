package com.github.timo_reymann.csv_parser.helper;

import com.github.timo_reymann.csv_parser.meta.CsvColumn;
import lombok.Data;

@Data
public class TestEntityWithNumericIndex {
    @CsvColumn(index = 0)
    private String someStringCol;

    @CsvColumn(index = 1)
    private Integer someIntCol;
}
