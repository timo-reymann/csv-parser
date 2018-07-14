package com.github.timo_reymann.csv_parser.test.meta;

import com.github.timo_reymann.csv_parser.meta.CsvMetaDataReader;
import com.github.timo_reymann.csv_parser.test.CsvParserTestCase;
import com.github.timo_reymann.csv_parser.test.helper.InvalidEntity;
import com.github.timo_reymann.csv_parser.test.helper.TestEntityWithHeadings;
import com.github.timo_reymann.csv_parser.test.helper.TestEntityWithNumericIndex;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;

public class CsvMetaDataReaderTest {
    @Test
    public void testHeadingsMapping() {
        CsvMetaDataReader<TestEntityWithHeadings> csvMetaDataReaderWithHeadings = new CsvMetaDataReader<>(TestEntityWithHeadings.class);
        HashMap<Object, Field> effectiveValueForColumnMapping = csvMetaDataReaderWithHeadings.getEffectiveValueForColumnMapping();

        Field someNumberCol = effectiveValueForColumnMapping.get("someNumberCol");
        Assert.assertNotNull(someNumberCol);
        Assert.assertEquals(Field.class, someNumberCol.getClass());
        Assert.assertEquals(Integer.class, someNumberCol.getType());
        Assert.assertTrue(someNumberCol.isAccessible());

        Field someTextCol = effectiveValueForColumnMapping.get("someTextCol");
        Assert.assertNotNull(someTextCol);
        Assert.assertEquals(Field.class, someTextCol.getClass());
        Assert.assertEquals(String.class, someTextCol.getType());
    }

    @Test
    public void testIntegerMapping() {
        CsvMetaDataReader<TestEntityWithNumericIndex> indexCsvMetaDataReader = new CsvMetaDataReader<>(TestEntityWithNumericIndex.class);
        HashMap<Object, Field> effectiveValueForColumnMapping = indexCsvMetaDataReader.getEffectiveValueForColumnMapping();

        Field someStringCol = effectiveValueForColumnMapping.get(0);
        Assert.assertNotNull(someStringCol);
        Assert.assertEquals(Field.class, someStringCol.getClass());
        Assert.assertEquals(String.class, someStringCol.getType());

        Field someIntCol = effectiveValueForColumnMapping.get(1);
        Assert.assertNotNull(someIntCol);
        Assert.assertEquals(Field.class, someIntCol.getClass());
        Assert.assertEquals(Integer.class, someIntCol.getType());
    }

    @Test(expected = CsvMetaDataReader.InvalidCsvColumnAnnotation.class)
    public void testInvalidMappingThrowsException() {
        new CsvMetaDataReader<>(InvalidEntity.class).getEffectiveValueForColumnMapping();
    }
}
