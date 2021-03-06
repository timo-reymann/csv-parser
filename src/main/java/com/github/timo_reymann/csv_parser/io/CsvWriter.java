package com.github.timo_reymann.csv_parser.io;

import com.github.timo_reymann.csv_parser.meta.CsvMetaDataReader;
import com.github.timo_reymann.csv_parser.util.Converter;
import com.github.timo_reymann.csv_parser.util.Platform;
import lombok.Data;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Write csv files
 *
 * @author Timo Reymann
 * @since 20.12.17
 */
@Data
public class CsvWriter<T> implements AutoCloseable, Closeable, Flushable {
    /**
     * Append to file instead of replacing it completely
     */
    private final boolean append;

    /**
     * File has csv headers
     */
    private final boolean hasHeadings;

    /**
     * Class for bean
     */
    private Class<T> clazz;

    /**
     * Meta data provider for bean
     */
    private CsvMetaDataReader<T> csvMetaDataReader;

    /**
     * File to save, this may be null if only stream is used
     */
    private File file;

    /**
     * Seperator for lines
     */
    private String seperator;

    /**
     * Buffered Writer to write to file
     */
    private BufferedWriter bufferedWriter;

    /**
     * Bool if csv writer has written data
     */
    private boolean hasWrittenData;

    /**
     * Converter api
     */
    private final Converter converter = new Converter();

    /**
     * Create new csv writer
     *
     * @param clazz       Class for bean
     * @param file        File to write
     * @param append      Append to output
     * @param hasHeadings Has the file headings, if this is a new file headers are automatically generated
     * @param seperator   Seperator for csv file
     * @throws IOException Error opening file streams
     */
    public CsvWriter(Class<T> clazz, File file, boolean append, boolean hasHeadings, String seperator) throws IOException {
        this(clazz, append, hasHeadings, seperator);
        initUsingFile(file);
    }

    /**
     * Create new csv writer
     *
     * @param clazz        Class for bean
     * @param outputStream OutputStream to write to
     * @param append       Append to output
     * @param hasHeadings  Has the file headings, if this is a new file headers are automatically generated
     * @param seperator    Seperator for csv file
     * @throws IOException Error opening file streams
     */
    public CsvWriter(Class<T> clazz, OutputStream outputStream, boolean append, boolean hasHeadings, String seperator) throws IOException {
        this(clazz, append, hasHeadings, seperator);
        initUsingStream(outputStream);
    }


    /**
     * Create new csv writer
     *
     * @param clazz       Class for bean
     * @param append      Append to output
     * @param hasHeadings Has the file headings, if this is a new file headers are automatically generated
     * @param seperator   Seperator for csv file
     */
    private CsvWriter(Class<T> clazz, boolean append, boolean hasHeadings, String seperator) {
        this.clazz = clazz;
        this.csvMetaDataReader = new CsvMetaDataReader<>(clazz);
        this.append = append;
        this.hasHeadings = hasHeadings;
        this.seperator = seperator;
    }

    /**
     * Map beans to string array, this method automatically decides to map by heading or by index
     *
     * @param bean Bean to map
     * @return String array with bean data mapped
     * @throws IllegalAccessException Error getting values from bean
     * @throws IOException            Error writing header
     */
    private String[] map(T bean) throws IllegalAccessException, IOException {
        if (hasHeadings) {
            return mapByHeading(bean);
        }
        return mapByIndex(bean);
    }

    /**
     * Map bean by index
     *
     * @param bean Bean
     * @return String array with data mapped according to specification over annotations
     * @throws IllegalAccessException Error getting values from bean
     */
    private String[] mapByIndex(T bean) throws IllegalAccessException {
        HashMap<Object, Field> effectiveMapping = csvMetaDataReader.getEffectiveValueForColumnMapping();
        String[] data = new String[effectiveMapping.size()];
        for (Map.Entry<Object, Field> entry : effectiveMapping.entrySet()) {
            data[(int) entry.getKey()] = formatValue(entry.getValue(), bean);
        }
        return data;
    }

    private String formatValue(Field field, Object obj) throws IllegalAccessException {
        Object value = field.get(obj);
        Class<?> type = field.getType();
        String format = csvMetaDataReader.getCsvColumnForField(field).format();

        if (type.isAssignableFrom(LocalDateTime.class)) {
            return converter.formatLocalDateTime(format, (LocalDateTime) value);
        } else if (type.isAssignableFrom(LocalDate.class)) {
            return converter.formatLocalDate(format, (LocalDate) value);
        } else {
            return value == null ? "" : value.toString();
        }
    }

    /**
     * Join list with strings to one single string separated by specified separator
     *
     * @param data List with strings to join
     * @return String
     */
    private String joinBySeperator(List<String> data) {
        return String.join(seperator, data);
    }

    /**
     * Write file header then headings are used for mapping and no file exists or file is empty
     *
     * @param headings Headings
     * @throws IOException Error writing header to file
     */
    private void writeFileHeader(List<String> headings) throws IOException {
        // If file is null, only input stream is used, and this will ALWAYS add headings to the output
        if (file != null) {
            if (file.length() > 0 || !hasHeadings) {
                return;
            }
        } else {
            if (hasWrittenData) {
                return;
            }
        }

        bufferedWriter.write('\uFEFF');
        writeRawData(headings);
        bufferedWriter.flush();
    }

    /**
     * Write raw string array to file
     *
     * @param data Raw data
     * @throws IOException Error writing to file
     */
    private void writeRawData(List<String> data) throws IOException {
        bufferedWriter.write(joinBySeperator(data) + Platform.getLineSeperator());
        hasWrittenData = true;
    }

    private String[] mapByHeading(T bean) throws IllegalAccessException, IOException {
        HashMap<Object, Field> effectiveMapping = csvMetaDataReader.getEffectiveValueForColumnMapping();

        List<String> list = new ArrayList<>();
        for (Map.Entry<Object, Field> objectFieldEntry : effectiveMapping.entrySet()) {
            Object key = objectFieldEntry.getKey();
            list.add(key.toString());
        }
        writeFileHeader(list);

        String[] data = new String[effectiveMapping.size()];
        for (Map.Entry<Object, Field> entry : effectiveMapping.entrySet()) {
            int index = list.indexOf(entry.getKey().toString());
            data[index] = formatValue(entry.getValue(), bean);
        }
        return data;
    }

    /**
     * Write only file headings to file without any data
     *
     * @throws IllegalAccessException Error getting fields of entity
     * @throws InstantiationException Error creating new instance of entity
     * @throws IOException            Error writing to file
     */
    public void writeFileHeading() throws IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException {
        if (isHasHeadings()) {
            mapByHeading(clazz.getConstructor().newInstance());
        }
    }

    /**
     * Write bean to file
     *
     * @param bean Bean to write
     * @throws IllegalAccessException Error getting value from object
     * @throws IOException            Error writing to file
     */
    public void writeLine(T bean) throws IllegalAccessException, IOException {
        writeRawData(Arrays.asList(map(bean)));
    }

    /**
     * Write beans to file
     *
     * @param beans Beans to write
     * @throws IllegalAccessException Error getting value from object
     * @throws IOException            Error writing to file
     */
    public void writeLine(List<T> beans) throws IOException, IllegalAccessException {
        for (T bean : beans) {
            writeLine(bean);
        }
    }

    /**
     * Init {@link BufferedWriter} with file
     *
     * @throws IOException Error open file
     */
    private void initUsingFile(File file) throws IOException {
        this.file = file;
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_8));
    }

    /**
     * Init {@link BufferedWriter} with output stream
     *
     * @param outputStream OutputStream to use
     */
    private void initUsingStream(OutputStream outputStream) {
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    /**
     * Flush underlying  {@link BufferedWriter} and {@link FileWriter}
     *
     * @throws IOException Error flushing
     */
    @Override
    public void flush() throws IOException {
        if (bufferedWriter != null)
            bufferedWriter.flush();
    }

    /**
     * Close underyling {@link BufferedWriter} and {@link FileWriter}
     *
     * @throws IOException Error close writers
     */
    @Override
    public void close() throws IOException {
        if (bufferedWriter != null)
            bufferedWriter.close();
    }

    public static class Builder<T> {
        /**
         * Append to file instead of replacing it completely
         */
        private boolean append = true;

        /**
         * File has csv headers
         */
        private boolean hasHeadings = false;

        /**
         * Class for bean
         */
        private Class<T> clazz;

        /**
         * File to save
         */
        private File file;

        /**
         * OutputStream to use over file
         */
        private OutputStream outputStream;

        /**
         * Seperator for lines
         */
        private String seperator = Seperator.SEMICOLON;

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
         * Set file to write tos
         *
         * @param file CSV file
         * @return Current builder
         */
        public Builder<T> file(File file) {
            this.file = file;
            return this;
        }

        /**
         * Set outputsream to write to
         *
         * @param outputStream OutputStream to use for writing
         * @return Current builder
         */
        public Builder<T> outputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
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
            this.hasHeadings = true;
            return this;
        }

        public Builder<T> noAppend() {
            this.append = false;
            return this;
        }

        public CsvWriter<T> build() throws IOException {
            if (this.outputStream != null && this.file != null) {
                throw new IllegalArgumentException("Decide if you want to use an outputstream or an file, both at the same time are not supported!");
            }

            if (this.outputStream == null) {
                return new CsvWriter<>(clazz, file, append, hasHeadings, seperator);
            } else {
                return new CsvWriter<>(clazz, outputStream, append, hasHeadings, seperator);
            }
        }

    }
}
