package com.github.timo_reymann.csv_parser.meta;

import com.github.timo_reymann.csv_parser.helper.InvalidEntity;
import com.github.timo_reymann.csv_parser.helper.TestEntityWithHeadings;
import com.github.timo_reymann.csv_parser.helper.TestEntityWithNumericIndex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;

public class CsvMetaDataReaderTest {
    @Test
    public void testHeadingsMapping() {
        CsvMetaDataReader<TestEntityWithHeadings> csvMetaDataReaderWithHeadings = new CsvMetaDataReader<>(TestEntityWithHeadings.class);
        HashMap<Object, Field> effectiveValueForColumnMapping = csvMetaDataReaderWithHeadings.getEffectiveValueForColumnMapping();

        Field someNumberCol = effectiveValueForColumnMapping.get("someNumberCol");
        Assertions.assertNotNull(someNumberCol);
        Assertions.assertEquals(Field.class, someNumberCol.getClass());
        Assertions.assertEquals(Integer.class, someNumberCol.getType());

        Field someTextCol = effectiveValueForColumnMapping.get("someTextCol");
        Assertions.assertNotNull(someTextCol);
        Assertions.assertEquals(Field.class, someTextCol.getClass());
        Assertions.assertEquals(String.class, someTextCol.getType());
    }

    @Test
    public void testIntegerMapping() {
        CsvMetaDataReader<TestEntityWithNumericIndex> indexCsvMetaDataReader = new CsvMetaDataReader<>(TestEntityWithNumericIndex.class);
        HashMap<Object, Field> effectiveValueForColumnMapping = indexCsvMetaDataReader.getEffectiveValueForColumnMapping();

        Field someStringCol = effectiveValueForColumnMapping.get(0);
        Assertions.assertNotNull(someStringCol);
        Assertions.assertEquals(Field.class, someStringCol.getClass());
        Assertions.assertEquals(String.class, someStringCol.getType());

        Field someIntCol = effectiveValueForColumnMapping.get(1);
        Assertions.assertNotNull(someIntCol);
        Assertions.assertEquals(Field.class, someIntCol.getClass());
        Assertions.assertEquals(Integer.class, someIntCol.getType());
    }

    @Test
    public void testInvalidMappingThrowsException() {
        Assertions.assertThrows(CsvMetaDataReader.InvalidCsvColumnAnnotation.class,
                () -> new CsvMetaDataReader<>(InvalidEntity.class).getEffectiveValueForColumnMapping());
    }
}
