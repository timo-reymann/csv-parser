package com.github.timo_reymann.csv_parser.test.io;

import com.github.timo_reymann.csv_parser.io.CsvWriter;
import com.github.timo_reymann.csv_parser.test.CsvParserTestCase;
import com.github.timo_reymann.csv_parser.test.helper.FileHelper;
import com.github.timo_reymann.csv_parser.test.helper.TestEntityWithHeadings;
import com.github.timo_reymann.csv_parser.test.helper.TestEntityWithNumericIndex;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CsvWriterTest extends CsvParserTestCase {
    private static File TMP_FILE_WRITE_HEADING = new File("tmp_heading.csv");
    private static File TMP_FILE_WRITE_NUMERIC = new File("tmp_numeric.csv");

    private CsvWriter<TestEntityWithNumericIndex> csvWriterNumericIndex;
    private CsvWriter<TestEntityWithHeadings> csvWriterHeadingIndex;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        csvWriterNumericIndex = new CsvWriter.Builder<TestEntityWithNumericIndex>()
                .file(TMP_FILE_WRITE_NUMERIC)
                .noAppend()
                .forClass(TestEntityWithNumericIndex.class)
                .build();

        csvWriterHeadingIndex = new CsvWriter.Builder<TestEntityWithHeadings>()
                .file(TMP_FILE_WRITE_HEADING)
                .noAppend()
                .forClass(TestEntityWithHeadings.class)
                .hasHeading()
                .build();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (TMP_FILE_WRITE_HEADING.exists()) {
            TMP_FILE_WRITE_HEADING.delete();
        }

        if (TMP_FILE_WRITE_NUMERIC.exists()) {
            TMP_FILE_WRITE_NUMERIC.delete();
        }
    }

    public void testWriteNumericIndex() throws IOException, IllegalAccessException {
        TestEntityWithNumericIndex testEntityWithNumericIndex = new TestEntityWithNumericIndex();

        for (int i = 1; i < 3; i++) {
            testEntityWithNumericIndex.setSomeIntCol(i);
            testEntityWithNumericIndex.setSomeStringCol("This is line " + i);
            csvWriterNumericIndex.writeLine(testEntityWithNumericIndex);
        }

        csvWriterNumericIndex.close();

        FileHelper.assertContentEquals(FileHelper.loadResourceFromTestClasspath("without_headings.csv"), TMP_FILE_WRITE_NUMERIC);
    }

    public void testWriteHeading() throws IOException, IllegalAccessException {
        TestEntityWithHeadings testEntityWithHeadings = new TestEntityWithHeadings();

        // Line 1
        testEntityWithHeadings.setSomeNumber(1);
        testEntityWithHeadings.setSomeText("This is line1");
        testEntityWithHeadings.setSomeBoolean(false);
        testEntityWithHeadings.setSomeDouble(45.0);
        testEntityWithHeadings.setSomeFloat(120.122f);
        testEntityWithHeadings.setLocalDate(LocalDate.of(2017, 6, 7));
        testEntityWithHeadings.setLocalDateTime(LocalDateTime.of(2018, 10, 2, 15, 30, 12));
        csvWriterHeadingIndex.writeLine(testEntityWithHeadings);

        // Line 2
        testEntityWithHeadings.setSomeNumber(2);
        testEntityWithHeadings.setSomeText("This is line2");
        testEntityWithHeadings.setSomeBoolean(true);
        testEntityWithHeadings.setSomeDouble(100.45);
        testEntityWithHeadings.setSomeFloat(120.122334f);
        testEntityWithHeadings.setLocalDate(LocalDate.of(2017, 8, 5));
        testEntityWithHeadings.setLocalDateTime(LocalDateTime.of(2017, 6, 4, 15, 10, 20));
        csvWriterHeadingIndex.writeLine(testEntityWithHeadings);

        csvWriterHeadingIndex.close();

        FileHelper.assertContentEquals(FileHelper.loadResourceFromTestClasspath("with_headings.csv"), TMP_FILE_WRITE_HEADING);
    }
}
