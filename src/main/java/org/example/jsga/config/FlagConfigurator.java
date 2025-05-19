package org.example.jsga.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads and manages runtime flags and parameters from jsga.properties.
 */
public class FlagConfigurator {

    private final Properties props;

    public FlagConfigurator(String configPath) throws IOException {
        props = new Properties();
        try (FileInputStream in = new FileInputStream(configPath)) {
            props.load(in);
        }
    }

    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(props.getProperty(key, Integer.toString(defaultValue)));
    }

    public long getLong(String key, long defaultValue) {
        return Long.parseLong(props.getProperty(key, Long.toString(defaultValue)));
    }

    public double getDouble(String key, double defaultValue) {
        return Double.parseDouble(props.getProperty(key, Double.toString(defaultValue)));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(props.getProperty(key, Boolean.toString(defaultValue)));
    }

    public String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    // Standard GA Option Flags
    public boolean isTraceEnabled() {
        return getBoolean("trace", false);
    }

    public boolean isLogEnabled() {
        return getBoolean("log", false);
    }

    public boolean isBestEnabled() {
        return getBoolean("best", true);
    }

    public String getOptions() {
        return getString("options", Constants.DEFAULT_OPTIONS);
    }
}
