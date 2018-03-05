package com.github.timo_reymann.csv_parser.test.io;

import com.github.timo_reymann.csv_parser.io.CsvWriter;
import com.github.timo_reymann.csv_parser.test.CsvParserTestCase;
import com.github.timo_reymann.csv_parser.test.helper.FileHelper;
import com.github.timo_reymann.csv_parser.test.helper.TestEntityWithHeadings;
import com.github.timo_reymann.csv_parser.test.helper.TestEntityWithNumericIndex;

import java.io.File;
import java.io.IOException;

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

        for (int i = 1; i < 3; i++) {
            testEntityWithHeadings.setSomeNumber(i);
            testEntityWithHeadings.setSomeText("This is line" + i);
            csvWriterHeadingIndex.writeLine(testEntityWithHeadings);
        }

        csvWriterHeadingIndex.close();

        FileHelper.assertContentEquals(FileHelper.loadResourceFromTestClasspath("with_headings.csv"),TMP_FILE_WRITE_HEADING);
    }
}
