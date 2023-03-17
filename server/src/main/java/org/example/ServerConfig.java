package org.example;

import java.io.*;
import java.util.Properties;
import java.util.Set;

public class ServerConfig {

    private String fileName;
    private Properties properties;

    public ServerConfig(String fileName) throws IOException {
        this.fileName = fileName;
        properties = new Properties();
        InputStream input = new FileInputStream(fileName);
        properties.load(input);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public Set<Object> getKeys() {
        return properties.keySet();
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        try (OutputStream output = new FileOutputStream(fileName)) {
            properties.store(output, null);
            System.out.printf("%s was set to %s%n", key, value);
        } catch (IOException e) {
            System.err.println("Error writing to config file");
        }
    }
}
