package me.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class FileReaderUtil {

    public static String readFileFromResources(String fileName) {
        StringBuilder content = new StringBuilder();

        // Get the class loader
        ClassLoader classLoader = FileReaderUtil.class.getClassLoader();

        // Open the file as a stream
        try (InputStream inputStream = classLoader.getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                throw new IOException("File not found: " + fileName);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return content.toString().trim(); // Trim to remove the last newline
    }
}
