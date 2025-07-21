package org.example.jsga.selection;

import org.example.jsga.model.Population;

@FunctionalInterface
public interface Selection {
    void select(Population from, Population to);
}
