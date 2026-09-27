package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Population;

@FunctionalInterface
public interface Selection {
    void select(Population from, Population to);
}
