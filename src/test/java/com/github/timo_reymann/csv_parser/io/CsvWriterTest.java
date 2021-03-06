package com.github.timo_reymann.csv_parser.io;

import com.github.timo_reymann.csv_parser.CsvParserTestCase;
import com.github.timo_reymann.csv_parser.helper.FileHelper;
import com.github.timo_reymann.csv_parser.helper.TestEntityWithHeadings;
import com.github.timo_reymann.csv_parser.helper.TestEntityWithNumericIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CsvWriterTest extends CsvParserTestCase {
    private static File TMP_FILE_WRITE_HEADING = new File("tmp_heading.csv");
    private static File TMP_FILE_WRITE_NUMERIC = new File("tmp_numeric.csv");

    private CsvWriter<TestEntityWithNumericIndex> csvWriterNumericIndex;
    private CsvWriter<TestEntityWithHeadings> csvWriterHeadingIndex;
    private CsvWriter<TestEntityWithHeadings> csvWriterWithStream;
    private CsvWriter<TestEntityWithHeadings> csvWriterHeadingsOnly;

    @BeforeEach
    protected void setUp() throws Exception {
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

        csvWriterHeadingsOnly = new CsvWriter.Builder<TestEntityWithHeadings>()
                .file(TMP_FILE_WRITE_HEADING)
                .seperatedBy("|")
                .noAppend()
                .forClass(TestEntityWithHeadings.class)
                .hasHeading()
                .build();

        csvWriterWithStream = new CsvWriter.Builder<TestEntityWithHeadings>()
                .outputStream(new FileOutputStream(TMP_FILE_WRITE_HEADING))
                .noAppend()
                .hasHeading()
                .forClass(TestEntityWithHeadings.class)
                .build();
    }

    @AfterEach
    protected void tearDown() throws Exception {
        if (TMP_FILE_WRITE_HEADING.exists()) {
            TMP_FILE_WRITE_HEADING.delete();
        }

        if (TMP_FILE_WRITE_NUMERIC.exists()) {
            TMP_FILE_WRITE_NUMERIC.delete();
        }
    }

    @Test
    public void testOutputStream() throws IllegalAccessException, IOException {
        writeFirstLineWithHeading(csvWriterWithStream);
        writeSecondLineWithHeading(csvWriterWithStream);
        csvWriterWithStream.flush();
        FileHelper.assertContentEquals(FileHelper.loadResourceFromTestClasspath("with_headings.csv"), TMP_FILE_WRITE_HEADING);
    }

    @Test
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

    @Test
    public void testHeadingWrite() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        csvWriterHeadingsOnly.writeFileHeading();
        csvWriterHeadingsOnly.close();
        FileHelper.assertContentEquals(FileHelper.loadResourceFromTestClasspath("headings_only.csv"), TMP_FILE_WRITE_HEADING);
    }

    private void writeFirstLineWithHeading(CsvWriter<TestEntityWithHeadings> csvWriterHeadingIndex) throws IOException, IllegalAccessException {
        TestEntityWithHeadings testEntityWithHeadings = new TestEntityWithHeadings();
        testEntityWithHeadings.setSomeNumber(1);
        testEntityWithHeadings.setSomeText("Thöis is line1");
        testEntityWithHeadings.setSomeBoolean(false);
        testEntityWithHeadings.setSomeDouble(45.0);
        testEntityWithHeadings.setSomeFloat(120.122f);
        testEntityWithHeadings.setLocalDate(LocalDate.of(2017, 6, 7));
        testEntityWithHeadings.setLocalDateTime(LocalDateTime.of(2018, 10, 2, 15, 30, 12));
        csvWriterHeadingIndex.writeLine(testEntityWithHeadings);
    }

    private void writeSecondLineWithHeading(CsvWriter<TestEntityWithHeadings> csvWriterHeadingIndex) throws IOException, IllegalAccessException {
        TestEntityWithHeadings testEntityWithHeadings = new TestEntityWithHeadings();
        testEntityWithHeadings.setSomeNumber(2);
        testEntityWithHeadings.setSomeText("Thöis is line2");
        testEntityWithHeadings.setSomeBoolean(true);
        testEntityWithHeadings.setSomeDouble(100.45);
        testEntityWithHeadings.setSomeFloat(120.122334f);
        testEntityWithHeadings.setLocalDate(LocalDate.of(2017, 8, 5));
        testEntityWithHeadings.setLocalDateTime(LocalDateTime.of(2017, 6, 4, 15, 10, 20));
        csvWriterHeadingIndex.writeLine(testEntityWithHeadings);
    }

    @Test
    public void testWriteHeading() throws IOException, IllegalAccessException {
        writeFirstLineWithHeading(csvWriterHeadingIndex);
        writeSecondLineWithHeading(csvWriterHeadingIndex);
        csvWriterHeadingIndex.close();
        FileHelper.assertContentEquals(FileHelper.loadResourceFromTestClasspath("with_headings.csv"), TMP_FILE_WRITE_HEADING);
    }
}
