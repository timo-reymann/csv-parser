package com.github.timo_reymann.csv_parser.test.io;

import com.github.timo_reymann.csv_parser.io.CsvReader;
import com.github.timo_reymann.csv_parser.test.CsvParserTestCase;
import com.github.timo_reymann.csv_parser.test.helper.FileHelper;
import com.github.timo_reymann.csv_parser.test.helper.TestEntityWithHeadings;
import com.github.timo_reymann.csv_parser.test.helper.TestEntityWithNumericIndex;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CsvReaderTest extends CsvParserTestCase {
    private CsvReader<TestEntityWithHeadings> csvReaderWithHeading;
    private CsvReader<TestEntityWithNumericIndex> csvReaderWithNumeric;

    @Override
    protected void setUp() throws Exception {
        csvReaderWithHeading = new CsvReader.Builder<TestEntityWithHeadings>()
                .file(FileHelper.loadResourceFromTestClasspath("with_headings.csv"))
                .forClass(TestEntityWithHeadings.class)
                .hasHeading()
                .build();

        csvReaderWithNumeric = new CsvReader.Builder<TestEntityWithNumericIndex>()
                .file(FileHelper.loadResourceFromTestClasspath("without_headings.csv"))
                .forClass(TestEntityWithNumericIndex.class)
                .build();
    }

    public void testMappingOfReadLineWithNumericIndex() throws IllegalAccessException, IOException, InstantiationException {
        TestEntityWithNumericIndex testEntityWithHeadings = csvReaderWithNumeric.readLine();

        assertNotNull(testEntityWithHeadings);
        assertEquals(new Integer(1), testEntityWithHeadings.getSomeIntCol());
        assertEquals("This is line 1", testEntityWithHeadings.getSomeStringCol());

        testEntityWithHeadings = csvReaderWithNumeric.readLine();
        assertEquals(new Integer(2), testEntityWithHeadings.getSomeIntCol());
        assertEquals("This is line 2", testEntityWithHeadings.getSomeStringCol());

        csvReaderWithHeading.flush();
    }

    public void testMappingOfStreamWithNumericIndex() {
        List<TestEntityWithNumericIndex> collect = csvReaderWithNumeric.lines().collect(Collectors.toList());

        for (int i = 0; i < collect.size(); i++) {
            TestEntityWithNumericIndex testEntityWithHeadings = collect.get(i);
            assertNotNull(collect);
            assertEquals(new Integer(i + 1), testEntityWithHeadings.getSomeIntCol());
            assertEquals("This is line " + (i + 1), testEntityWithHeadings.getSomeStringCol());
        }

    }


    public void testMappingOfReadLineWithHeading() throws IllegalAccessException, IOException, InstantiationException {
        TestEntityWithHeadings testEntityWithHeadings = csvReaderWithHeading.readLine();

        assertNotNull(testEntityWithHeadings);
        assertEquals(new Integer(1), testEntityWithHeadings.getSomeNumber());
        assertEquals("This is line1", testEntityWithHeadings.getSomeText());

        testEntityWithHeadings = csvReaderWithHeading.readLine();
        assertEquals(new Integer(2), testEntityWithHeadings.getSomeNumber());
        assertEquals("This is line2", testEntityWithHeadings.getSomeText());

        csvReaderWithHeading.flush();
    }

    public void testMappingOfStreamWithHeading() {
        List<TestEntityWithHeadings> collect = csvReaderWithHeading.lines().collect(Collectors.toList());

        for (int i = 0; i < collect.size(); i++) {
            TestEntityWithHeadings testEntityWithHeadings = collect.get(i);
            assertNotNull(collect);
            assertEquals(new Integer(i + 1), testEntityWithHeadings.getSomeNumber());
            assertEquals("This is line" + (i + 1), testEntityWithHeadings.getSomeText());
        }

    }
}
