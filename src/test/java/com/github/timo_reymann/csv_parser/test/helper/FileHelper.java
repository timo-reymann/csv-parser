package com.github.timo_reymann.csv_parser.test.helper;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FileHelper {
    public static File loadResourceFromTestClasspath(String fileName) {
        return new File(FileHelper.class.getClassLoader().getResource(fileName).getFile());
    }

    public static void assertContentEquals(File expectedFile, File actualFile) throws IOException {
        BufferedReader expected = new BufferedReader(new FileReader(expectedFile));
        BufferedReader actual = new BufferedReader(new FileReader(actualFile));

        String line;
        while ((line = expected.readLine()) != null) {
            assertEquals(line, actual.readLine());
        }

        assertNull("Actual had more lines then the expected.", actual.readLine());
        assertNull("Expected had more lines then the actual.", expected.readLine());
    }
}
