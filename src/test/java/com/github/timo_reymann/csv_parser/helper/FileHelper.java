package com.github.timo_reymann.csv_parser.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class FileHelper {
    public static File loadResourceFromTestClasspath(String fileName) {
        return new File(FileHelper.class.getClassLoader().getResource(fileName).getFile());
    }

    public static void assertContentEquals(File expectedFile, File actualFile) throws IOException {
        BufferedReader expected = new BufferedReader(new FileReader(expectedFile));
        BufferedReader actual = new BufferedReader(new FileReader(actualFile));

        String line;
        while ((line = expected.readLine()) != null) {
            assertEquals(line, actual.readLine().replace("\uFEFF", ""));
        }

        assertNull(actual.readLine(), "Actual had more lines then the expected.");
        assertNull(expected.readLine(), "Expected had more lines then the actual.");
    }
}
