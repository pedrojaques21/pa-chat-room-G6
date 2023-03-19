package org.example;

import java.io.*;
import java.util.Properties;
import java.util.Set;

/**
 * ServerConfig class represents the class that will handle all operations with the server.config file
 */
public class ServerConfig {

    private String fileName;
    private Properties properties;

    /**
     * The constructor receives the file path and sets the properties to the server configs
     *
     * @param fileName is the file path where the file is located
     * @throws IOException because there might be a wrong location to the file
     */
    public ServerConfig(String fileName) throws IOException {
        this.fileName = fileName;
        properties = new Properties();
        InputStream input = new FileInputStream(fileName);
        properties.load(input);
    }

    /**
     * This method returns the value of a configuration set on the server.config file
     *
     * @param key is the corresponding configuration name
     * @return
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * This method returns all the configurations name set on de server.config file
     *
     * @return a set of configuration names
     */
    public Set<Object> getKeys() {
        return properties.keySet();
    }

    /**
     * This method sets the value for a configuration on the server.config file
     *
     * @param key is the configuration name
     * @param value is the new value to set on the key
     */
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
