package com.github.timo_reymann.csv_parser.io;

import com.github.timo_reymann.csv_parser.meta.CsvMetaDataReader;
import com.github.timo_reymann.csv_parser.util.Platform;
import lombok.Data;

import java.io.*;
import java.lang.reflect.Field;
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
     * File to save
     */
    private File file;

    /**
     * Seperator for lines
     */
    private String seperator = Seperator.SEMICOLON;

    /**
     * File writer instance
     */
    private FileWriter fileWriter;

    /**
     * Buffered Writer to write to file
     */
    private BufferedWriter bufferedWriter;

    /**
     * Create new csv writer
     *
     * @param clazz       Class for bean
     * @param file        File to write
     * @param append      Append to output
     * @param hasHeadings Has the file headings, if this is a new file headers are automatically generated
     * @param seperator
     * @throws IOException Error opening file streams
     */
    public CsvWriter(Class<T> clazz, File file, boolean append, boolean hasHeadings, String seperator) throws IOException {
        this.clazz = clazz;
        this.file = file;
        this.csvMetaDataReader = new CsvMetaDataReader<>(clazz);
        this.append = append;
        this.hasHeadings = hasHeadings;
        this.seperator = seperator;
        init();
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
            data[(int) entry.getKey()] = String.valueOf(entry.getValue().get(bean));
        }
        return data;
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
        if (file.length() > 0 || !hasHeadings)
            return;

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
            data[index] = entry.getValue().get(bean).toString();
        }
        return data;
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
     * Init {@link BufferedWriter} and {@link FileWriter}
     *
     * @throws IOException Error open file
     */
    private void init() throws IOException {
        fileWriter = new FileWriter(file, append);
        bufferedWriter = new BufferedWriter(fileWriter);
    }

    /**
     * Flush underlying  {@link BufferedWriter} and {@link FileWriter}
     *
     * @throws IOException Error flushing
     */
    @Override
    public void flush() throws IOException {
        if (fileWriter != null)
            fileWriter.flush();

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

        if (fileWriter != null)
            fileWriter.close();
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
            this.hasHeadings = true;
            return this;
        }

        public Builder<T> noAppend() {
            this.append = false;
            return this;
        }

        public CsvWriter<T> build() throws IOException {
            return new CsvWriter<>(clazz, file, append, hasHeadings, seperator);
        }

    }
}
