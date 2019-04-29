package com.github.timo_reymann.csv_parser.test.io;

import com.github.timo_reymann.csv_parser.exception.ParseException;
import com.github.timo_reymann.csv_parser.io.CsvReader;
import com.github.timo_reymann.csv_parser.io.Seperator;
import com.github.timo_reymann.csv_parser.test.CsvParserTestCase;
import com.github.timo_reymann.csv_parser.test.helper.FileHelper;
import com.github.timo_reymann.csv_parser.test.helper.TestEntityWithHeadings;
import com.github.timo_reymann.csv_parser.test.helper.TestEntityWithNumericIndex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CsvReaderTest extends CsvParserTestCase {
    private CsvReader<TestEntityWithHeadings> csvReaderWithHeading;
    private CsvReader<TestEntityWithNumericIndex> csvReaderWithNumeric;
    private CsvReader<TestEntityWithHeadings> csvReaderUsingStream;
    private CsvReader<TestEntityWithHeadings> csvReaderDifferentSeperator;

    @BeforeEach
    protected void beforeEach() throws Exception {
        csvReaderWithHeading = new CsvReader.Builder<TestEntityWithHeadings>()
                .file(FileHelper.loadResourceFromTestClasspath("with_headings.csv"))
                .forClass(TestEntityWithHeadings.class)
                .seperatedBy(";")
                .hasHeading()
                .build();

        csvReaderDifferentSeperator = new CsvReader.Builder<TestEntityWithHeadings>()
                .file(FileHelper.loadResourceFromTestClasspath("headings_diff_seperator.csv"))
                .forClass(TestEntityWithHeadings.class)
                .seperatedBy("|")
                .hasHeading()
                .build();

        csvReaderWithNumeric = new CsvReader.Builder<TestEntityWithNumericIndex>()
                .file(FileHelper.loadResourceFromTestClasspath("without_headings.csv"))
                .forClass(TestEntityWithNumericIndex.class)
                .build();

        csvReaderUsingStream = new CsvReader.Builder<TestEntityWithHeadings>()
                .forClass(TestEntityWithHeadings.class)
                .hasHeading()
                .seperatedBy(";")
                .inputStream(new FileInputStream(FileHelper.loadResourceFromTestClasspath("with_headings.csv")))
                .build();
    }

    @Test
    public void testReadDifferentSeperator() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        TestEntityWithHeadings testEntityWithHeadings = csvReaderDifferentSeperator.readLine();

        Assertions.assertEquals(testEntityWithHeadings.getSomeNumber(),1);
    }

    @Test
    public void testInputStreamUsage() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        testFirstLineWithHeadings(csvReaderUsingStream.readLine());
        testSecondLineWithHeadings(csvReaderUsingStream.readLine());
    }

    @Test
    public void testMappingOfReadLineWithNumericIndex() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        TestEntityWithNumericIndex testEntityWithHeadings = csvReaderWithNumeric.readLine();

        assertNotNull(testEntityWithHeadings);
        assertEquals(Integer.valueOf(1), testEntityWithHeadings.getSomeIntCol());
        assertEquals("This is line 1", testEntityWithHeadings.getSomeStringCol());

        testEntityWithHeadings = csvReaderWithNumeric.readLine();
        assertEquals(Integer.valueOf(2), testEntityWithHeadings.getSomeIntCol());
        assertEquals("This is line 2", testEntityWithHeadings.getSomeStringCol());
    }

    @Test
    public void testMappingOfStreamWithNumericIndex() {
        List<TestEntityWithNumericIndex> collect = csvReaderWithNumeric.lines().collect(Collectors.toList());

        for (int i = 0; i < collect.size(); i++) {
            TestEntityWithNumericIndex testEntityWithHeadings = collect.get(i);
            assertNotNull(collect);
            assertEquals(Integer.valueOf(i + 1), testEntityWithHeadings.getSomeIntCol());
            assertEquals("This is line " + (i + 1), testEntityWithHeadings.getSomeStringCol());
        }

    }

    @Test
    public void testMappingOfReadLineWithHeading() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        testFirstLineWithHeadings(csvReaderWithHeading.readLine());
        testSecondLineWithHeadings(csvReaderWithHeading.readLine());
    }

    private void testSecondLineWithHeadings(TestEntityWithHeadings testEntityWithHeadings) {
        assertEquals(new Integer(2), testEntityWithHeadings.getSomeNumber());
        assertEquals("Thöis is line2", testEntityWithHeadings.getSomeText());
        assertEquals(Boolean.TRUE, testEntityWithHeadings.getSomeBoolean());
        assertEquals(100.45, testEntityWithHeadings.getSomeDouble());
        assertEquals(120.122334679f, testEntityWithHeadings.getSomeFloat());
        assertEquals(LocalDate.of(2017, 8, 5), testEntityWithHeadings.getLocalDate());
        assertEquals(LocalDateTime.of(2017, 6, 4, 15, 10, 20), testEntityWithHeadings.getLocalDateTime());
    }

    private void testFirstLineWithHeadings(TestEntityWithHeadings testEntityWithHeadings) {
        assertNotNull(testEntityWithHeadings);
        assertEquals(Integer.valueOf(1), testEntityWithHeadings.getSomeNumber());
        assertEquals("Thöis is line1", testEntityWithHeadings.getSomeText());
        assertEquals(Boolean.FALSE, testEntityWithHeadings.getSomeBoolean());
        assertEquals(45.0, testEntityWithHeadings.getSomeDouble());
        assertEquals(120.122f, testEntityWithHeadings.getSomeFloat());
        assertEquals(LocalDate.of(2017, 6, 7), testEntityWithHeadings.getLocalDate());
        assertEquals(LocalDateTime.of(2018, 10, 2, 15, 30, 12), testEntityWithHeadings.getLocalDateTime());
    }

    @Test
    public void testMappingOfStreamWithHeading() {
        List<TestEntityWithHeadings> collect = csvReaderWithHeading.lines().collect(Collectors.toList());

        for (int i = 0; i < collect.size() - 1; i++) {
            TestEntityWithHeadings testEntityWithHeadings = collect.get(i);
            assertNotNull(collect);
            assertEquals(Integer.valueOf(i + 1), testEntityWithHeadings.getSomeNumber());
            assertEquals("Thöis is line" + (i + 1), testEntityWithHeadings.getSomeText());
        }
    }


    @Test
    public void testErrorHandling() throws IllegalAccessException, IOException, InstantiationException, ParseException {

        CsvReader<TestEntityWithNumericIndex> readerForInvalidFile = new CsvReader.Builder<TestEntityWithNumericIndex>()
                .file(FileHelper.loadResourceFromTestClasspath("invalid_data.csv"))
                .forClass(TestEntityWithNumericIndex.class)
                .seperatedBy(Seperator.SEMICOLON)
                .build();

        try {
            readerForInvalidFile.readLine();
        } catch (ParseException | NoSuchMethodException | InvocationTargetException e) {
            assertNotNull(e);
            assertNotNull(e.getMessage());
        }
    }
}
