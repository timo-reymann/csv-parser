package com.github.timo_reymann.csv_parser.test.helper;

import com.github.timo_reymann.csv_parser.meta.CsvColumn;
import lombok.Data;

@Data
public class TestEntityWithHeadings {
    @CsvColumn(headerName = "someNumberCol")
    private Integer someNumber;

    @CsvColumn(headerName = "someTextCol")
    private String someText;
}
