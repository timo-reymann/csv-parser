package com.github.timo_reymann.csv_parser.meta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Csv meta data reader, used for reading meta information from bean class
 *
 * @author Timo Reymann
 * @since 20.12.17
 */
public class CsvMetaDataReader<T> {
    /**
     * Cache for mapping of csv columns to fields
     */
    private static HashMap<Class, HashMap<Field, CsvColumn>> CACHE = new HashMap<>();
    /**
     * Class object of entity
     */
    private Class<T> clazz;
    /**
     * Mapping of csv columns to fields
     */
    private HashMap<Field, CsvColumn> fields;

    /**
     * Create new meta data reader
     *
     * @param clazz Class object for bean to map from and to csv
     */
    public CsvMetaDataReader(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Get field csv column mapping
     *
     * @return Map with fields
     */
    private HashMap<Field, CsvColumn> getFieldsToMap() {
        HashMap<Field, CsvColumn> csvColumnFieldHashMap = CACHE.get(clazz);

        if (csvColumnFieldHashMap != null) {
            return csvColumnFieldHashMap;
        }

        fields = new HashMap<>();
        process(clazz.getDeclaredFields());
        process(clazz.getSuperclass().getDeclaredFields());
        CACHE.put(clazz, fields);
        return fields;
    }

    public CsvColumn getCsvColumnForField(Field field) {
        return getFieldsToMap().get(field);
    }

    /**
     * Get effective value for column mapping
     *
     * @return HashMap with fields indexed by Effective index (headerName/index)
     */
    public HashMap<Object, Field> getEffectiveValueForColumnMapping() {
        HashMap<Field, CsvColumn> fieldsToMap = getFieldsToMap();
        HashMap<Object, Field> effective = new HashMap<>();


        for (Map.Entry<Field, CsvColumn> es : fieldsToMap.entrySet()) {
            effective.put(getEffectiveValueForColumnMapping(es.getValue()), es.getKey());
        }

        return effective;
    }

    /**
     * Get effective value for column mapping  (headerName/index)
     *
     * @param column Column annotation
     * @return headerName ({@link String}) or index ({@link Integer})
     */
    private Object getEffectiveValueForColumnMapping(CsvColumn column) {
        if (column.index() == -1) {
            return column.headerName();
        } else {
            return column.index();
        }
    }

    /**
     * process fields list
     *
     * @param fields Fields of bean
     */
    private void process(Field[] fields) {
        for (Field field : fields) {
            process(field);
        }
    }

    /**
     * Validate column mapping annotation
     *
     * @param column Column annotation
     */
    private void validate(CsvColumn column) {
        if (column.index() == -1 && column.headerName().isEmpty()) {
            throw new InvalidCsvColumnAnnotation(column, "Please specifiy a column index or an header name to map the field on");
        }

        if (column.index() != -1 && column.index() < 0) {
            throw new InvalidCsvColumnAnnotation(column, "Index for column to map cant be negative");
        }
    }

    /**
     * Process field and its annotation, including validation
     *
     * @param field Field to process
     */
    private void process(Field field) {
        CsvColumn csvColumn = readMeta(field);

        if (csvColumn == null) {
            return;
        }

        validate(csvColumn);
        field.setAccessible(true);
        fields.put(field, csvColumn);
    }

    /**
     * Read annotation from field
     *
     * @param field Field to read
     * @return annotation object or null if not present
     */
    private CsvColumn readMeta(Field field) {
        return field.getDeclaredAnnotation(CsvColumn.class);
    }

    /**
     * Invalid csv column annotation value
     */
    public static class InvalidCsvColumnAnnotation extends RuntimeException {
        /**
         * Create new instance.
         *
         * @param csvColumn Column annotation
         * @param message   Error message
         */
        InvalidCsvColumnAnnotation(CsvColumn csvColumn, String message) {
            super("com.github.timo_reymann.csv_parser.meta.CsvColumn annotation " + csvColumn + "is not valid: " + message);
        }
    }
}
