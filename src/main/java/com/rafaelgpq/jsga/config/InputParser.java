package com.rafaelgpq.jsga.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads GA configuration from a properties file.
 */
public class InputParser {

    private final Properties config = new Properties();

    public void load(String path) throws IOException {
        try (FileInputStream in = new FileInputStream(path)) {
            config.load(in);
        }
    }

    public String get(String key) {
        return config.getProperty(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(config.getProperty(key, String.valueOf(defaultValue)));
    }

    public double getDouble(String key, double defaultValue) {
        return Double.parseDouble(config.getProperty(key, String.valueOf(defaultValue)));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(config.getProperty(key, String.valueOf(defaultValue)));
    }

    public Properties getProperties() {
        return config;
    }
}

