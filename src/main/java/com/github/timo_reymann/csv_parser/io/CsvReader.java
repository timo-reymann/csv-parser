package com.github.timo_reymann.csv_parser.io;

import com.github.timo_reymann.csv_parser.exception.ParseException;
import com.github.timo_reymann.csv_parser.meta.CsvMetaDataReader;
import com.github.timo_reymann.csv_parser.util.Converter;
import lombok.AccessLevel;
import lombok.Setter;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Reader for csv files
 *
 * @author Timo Reymann
 * @since 20.12.17
 */
public class CsvReader<T> implements AutoCloseable, Flushable, Closeable {
    /**
     * Underlying file reader
     */
    @Setter(AccessLevel.PROTECTED)
    private InputStreamReader inputStream;

    /**
     * Buffered reader for reading lines
     */
    @Setter(AccessLevel.PROTECTED)
    private BufferedReader bufferedReader;

    /**
     * Class object for type to read from file
     */
    @Setter(AccessLevel.PROTECTED)
    private Class<T> clazz;

    /**
     * Csv file has header in first line
     */
    private boolean hasHeading = false;

    /**
     * Seperator for csv file
     */
    private String seperator = Seperator.SEMICOLON;
    /**
     * Headings for file, if headings detection is enabled for this file
     */
    private HashMap<Integer, String> headings = new HashMap<>();
    /**
     * Meta data api
     */
    private CsvMetaDataReader<T> csvMetaDataReader;

    /**
     * Converter api
     */
    private final Converter converter = new Converter();

    /**
     * Mark if file has been read
     */
    private boolean hasReadData = false;

    /**
     * Create CsvReader
     *
     * @param file       File to read
     * @param clazz      Class of bean to read
     * @param hasHeading Has the file headers for column names
     * @throws FileNotFoundException File was not found on disk
     */
    public CsvReader(File file, Class<T> clazz, boolean hasHeading) throws FileNotFoundException {
        this(clazz, hasHeading);
        initUsingFile(file);
    }

    public CsvReader(InputStream inputStream, Class<T> clazz, boolean hasHeading) {
        this(clazz, hasHeading);
        initUsingInputStream(inputStream);
    }

    /**
     * Create CsvReader
     *
     * @param clazz      Class of bean to read
     * @param hasHeading Has the file headers for column names
     */
    private CsvReader(Class<T> clazz, boolean hasHeading) {
        this.clazz = clazz;
        this.csvMetaDataReader = new CsvMetaDataReader<>(clazz);
        this.setHasHeading(hasHeading);
    }

    /**
     * Create new instance
     *
     * @param fileName Name of file
     * @param clazz    Class of bean
     * @throws FileNotFoundException File was not found on disk
     * @deprecated Deprecated due to implicit boolean parameter
     */
    @Deprecated
    public CsvReader(String fileName, Class<T> clazz) throws FileNotFoundException {
        this(new File(fileName), clazz, false);
    }

    /**
     * Create new instance
     *
     * @param fileName   Name of file
     * @param clazz      Class of bean
     * @param hasHeading File has first line with headings
     * @throws FileNotFoundException File was not found on disk
     */
    public CsvReader(String fileName, Class<T> clazz, boolean hasHeading) throws FileNotFoundException {
        this(new File(fileName), clazz, hasHeading);
    }

    /**
     * Map splitted data of line to object of bean
     *
     * @param data Data to map
     * @return Mapped object
     * @throws InstantiationException Error creating bean instance, this occurs when
     *                                no default constructor without parameters is available or an exception is thrown during initalization
     * @throws IllegalAccessException Constructor is private
     */
    private T map(String[] data) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (hasHeading) {
            return mapByHeading(data);
        }
        return mapByIndex(data);
    }

    /**
     * Map bean by heading in annotations
     *
     * @param data Data to set
     * @return Mapped bean instance
     * @throws IllegalAccessException Constructor is private
     * @throws InstantiationException Error creating bean instance, this occurs when
     *                                no default constructor without parameters is available or an exception is thrown during initalization
     */
    private T mapByHeading(String[] data) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        T obj = clazz.getConstructor().newInstance();
        HashMap<Object, Field> effectiveValueForColumnMapping = csvMetaDataReader.getEffectiveValueForColumnMapping();
        for (Map.Entry<Integer, String> headingEntry : headings.entrySet()) {
            String val;
            try {
                val = data[headingEntry.getKey()];
            } catch (ArrayIndexOutOfBoundsException e) { // No element found
                val = null;
            }

            Field field = effectiveValueForColumnMapping.get(headingEntry.getValue());

            // Ignore if field is not mapped
            if (field != null) {
                setValue(field, obj, val);
            }
        }
        return obj;
    }

    /**
     * Set value for field, this automatically picks the type for the field
     *
     * @param field Field
     * @param obj   Object to set field in
     * @param value Value to set to field
     */
    private void setValue(Field field, Object obj, String value) {
        Class<?> type = field.getType();
        Function<String, Object> mapper = null;
        // Basic types
        if (type.isAssignableFrom(String.class)) {
            mapper = String::valueOf;
        } else if (type.isAssignableFrom(Integer.class)) {
            mapper = converter::convertToInt;
        } else if (type.isAssignableFrom(Boolean.class)) {
            mapper = converter::convertToBoolean;
        } else if (type.isAssignableFrom(Double.class)) {
            mapper = converter::convertToDouble;
        } else if (type.isAssignableFrom(Float.class)) {
            mapper = converter::convertToFloat;
        } else if (type.isAssignableFrom(LocalDate.class)) {
            mapper = (input) -> converter.convertToLocalDate(getFormatForColumn(field), input);
        } else if (type.isAssignableFrom(LocalDateTime.class)) {
            mapper = (input) -> converter.convertToLocalDateTime(getFormatForColumn(field), input);
        } else {
            try {
                // 'Castable' types, may produce error
                field.set(obj, field.getType().cast(value));
            } catch (Exception e) {
                throw new ParseException("Error parsing value '" + value + "'. Assigment for field failed", e);
            }

            // Leave method because no mapper is assigned
            return;
        }

        try {
            converter.setField(field, obj, mapper.apply(value));
        } catch (Exception e) {
            throw new ParseException("Error assigning value '" + value + "' to field " + field.getName() + "", e);
        }
    }

    private String getFormatForColumn(Field field) {
        try {
            return csvMetaDataReader.getCsvColumnForField(field).format();
        } catch (Exception e) {
            throw new InvalidArgumentException("format", "Format for column must be set if the datatype requires a format");
        }
    }

    /**
     * Map bean by index specified in annotation
     *
     * @param data Data to set
     * @return Mapped bean
     * @throws IllegalAccessException Constructor is private
     * @throws InstantiationException Error creating bean instance, this occurs when
     *                                no default constructor without parameters is available or an exception is thrown during initalization
     */
    private T mapByIndex(String[] data) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        T obj = clazz.getConstructor().newInstance();
        HashMap<Object, Field> effectiveMapping = csvMetaDataReader.getEffectiveValueForColumnMapping();
        for (int i = 0, dataLength = data.length; i < dataLength; i++) {
            Field field = effectiveMapping.get(i);

            if (field != null) {
                setValue(field, obj, data[i]);
            }
        }
        return obj;
    }

    /**
     * Read line from underlying {@link BufferedReader} and split it by seperator
     *
     * @return String array with columns split by seperator
     * @throws IOException Error reading line
     */
    private String[] readLineAndSplitBySeperator() throws IOException {
        if (hasReadData) {
            return splitBySeperator(bufferedReader.readLine());
        } else {
            hasReadData = true;
            String line = bufferedReader.readLine();
            return splitBySeperator(line.replace("\uFEFF", ""));
        }
    }

    /**
     * Split string by seperator
     *
     * @param line Line to split
     * @return String array with data
     */
    private String[] splitBySeperator(String line) {
        return line.split(seperator);
    }

    /**
     * Get heading from file, this automatically skips the first line for further processing
     *
     * @throws IOException Error reading first line
     */
    private void getHeadings() throws IOException {
        if (!hasHeading) {
            return;
        }

        headings.clear();
        String[] headingStrings = readLineAndSplitBySeperator();
        for (int i = 0; i < headingStrings.length; i++) {
            headings.put(i, headingStrings[i]);
        }
    }

    /**
     * Initialize input stream and {@link BufferedReader} for specified file
     *
     * @param file File
     * @throws FileNotFoundException File was not found on disk
     */
    private void initUsingFile(File file) throws FileNotFoundException {
        inputStream = new FileReader(file);
        bufferedReader = new BufferedReader(inputStream);
        try {
            getHeadings();
        } catch (IOException e) {
            throw new FileNotFoundException();
        }
    }

    /**
     * Initialize
     *
     * @param inputStream Input stream to use
     */
    private void initUsingInputStream(InputStream inputStream) {
        this.inputStream = new InputStreamReader(inputStream);
        this.bufferedReader = new BufferedReader(this.inputStream);
        try {
            getHeadings();
        } catch (IOException e) {
            throw new IllegalArgumentException("InputStream does not contain headings");
        }
    }

    /**
     * Close {@link FileReader} and {@link BufferedReader} instance
     *
     * @throws IOException Error while trying to close readers
     */
    public void close() throws IOException {
        this.hasReadData = false;

        if (this.inputStream != null) {
            this.inputStream.close();
        }

        if (this.bufferedReader != null) {
            this.bufferedReader.close();
        }
    }

    /**
     * Read line from csv file
     *
     * @return Mapped bean
     * @throws IOException            Error reading line from csv file
     * @throws IllegalAccessException Error mapping fields
     * @throws InstantiationException Error creating new bean instance for mapping
     */
    public T readLine() throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return map(readLineAndSplitBySeperator());
    }

    /**
     * Get stream with remaining lines from {@link BufferedReader} already mapped to beans,
     * if an error occurred during mapping, its element in list is null
     *
     * @return Stream with mapped bean objects
     */
    public Stream<T> lines() {
        return bufferedReader.lines()
                .map(l -> {
                    try {
                        return map(splitBySeperator(l));
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        // Ignore Exception
                    }
                    return null;
                });
    }

    /**
     * Flush {@link FileReader} and {@link BufferedReader}
     *
     * @deprecated Flush will be removed due to support for direct reading from an file stream, where flush is not reliable anymore
     */
    @Override
    @Deprecated
    public void flush() {
        this.bufferedReader = new BufferedReader(inputStream);
    }

    /**
     * Set has heading property
     *
     * @param hasHeading Has heading (true) or not (false)
     */
    public void setHasHeading(boolean hasHeading) {
        this.hasHeading = hasHeading;
    }

    /**
     * Set separator for splitting rows
     *
     * @param seperator Seperator
     */
    public void setSeperator(String seperator) {
        this.seperator = seperator;
    }

    /**
     * Builder for easily creating csv reader
     *
     * @param <T> Type of bean saved in csv file
     */
    public static class Builder<T> {
        /**
         * Final csv reader object
         */
        private CsvReader<T> csvReader;

        /**
         * Class of bean
         */
        private Class<T> clazz;

        /**
         * Csv file
         */
        private File file;

        /**
         * Input stream
         */
        private InputStream inputStream;

        /**
         * The file has a first line with headings
         */
        private boolean hasHeading = false;

        /**
         * Separator for file
         */
        private String seperator;

        /**
         * Set class of bean
         *
         * @param clazz Class object for bean
         * @return Current builder
         */
        public Builder<T> forClass(Class<T> clazz) {
            this.clazz = clazz;
            return this;
        }

        /**
         * Set file to read from
         *
         * @param file CSV file
         * @return Current builder
         */
        public Builder<T> file(File file) {
            this.file = file;
            return this;
        }

        /**
         * Set seperator for csv reader, default it is set to ';'
         *
         * @param seperator Seperator
         * @return Current builder
         */
        public Builder<T> seperatedBy(String seperator) {
            this.seperator = seperator;
            return this;
        }

        /**
         * File to read has first row with heading
         *
         * @return Current builder
         */
        public Builder<T> hasHeading() {
            this.hasHeading = true;
            return this;
        }

        /**
         * Set the input stream to use instead of file
         *
         * @param inputStream InputStream to use
         * @return Current builder
         */
        public Builder<T> inputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        /**
         * Build csv reader instance
         *
         * @return Ready to use csv reader
         */
        public CsvReader<T> build() {
            if (inputStream != null && file != null) {
                throw new IllegalArgumentException("Decide to use inputStream or file, both at the same time are not supported");
            }

            if (file != null) {
                if (!file.exists()) {
                    throw new InvalidArgumentException("file", file);
                }
            }

            if (clazz == null) {
                throw new InvalidArgumentException("class", clazz);
            }

            try {
                if (inputStream != null) {
                    csvReader = new CsvReader<>(inputStream, clazz, hasHeading);
                } else {
                    csvReader = new CsvReader<>(file, clazz, hasHeading);
                }
            } catch (FileNotFoundException e) {
                throw new InvalidArgumentException("file", e);
            }

            csvReader.setHasHeading(this.hasHeading);

            if (this.seperator != null && !this.seperator.isEmpty()) {
                csvReader.setSeperator(seperator);
            }

            return csvReader;
        }
    }
}
