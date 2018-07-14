package com.github.timo_reymann.csv_parser.test.helper;

import com.github.timo_reymann.csv_parser.meta.CsvColumn;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TestEntityWithHeadings {
    @CsvColumn(headerName = "someBoolCol")
    private Boolean someBoolean;

    @CsvColumn(headerName = "someNumberCol")
    private Integer someNumber;

    @CsvColumn(headerName = "someDate",format = "yyyy-MM-dd")
    private LocalDate localDate;

    @CsvColumn(headerName = "someDoubleCol")
    private Double someDouble;

    @CsvColumn(headerName = "someTextCol")
    private String someText;

    @CsvColumn(headerName = "someFloatCol")
    private Float someFloat;

    @CsvColumn(headerName = "someDateTime",format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime localDateTime;

    @CsvColumn(headerName = "empty")
    private String empty;

}
