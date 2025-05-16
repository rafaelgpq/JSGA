package org.example.jsga.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Central store for GA runtime flags (set once, read many).
 */
public class FlagConfigurator {

    private final Map<String, Boolean> flags = new HashMap<>();

    public void set(String flag, boolean value) {
        flags.put(flag, value);
    }

    public boolean get(String flag) {
        return flags.getOrDefault(flag, false);
    }

    public void toggle(String flag) {
        flags.put(flag, !get(flag));
    }

    public void reset() {
        flags.clear();
    }

    public Map<String, Boolean> getAll() {
        return flags;
    }
}
