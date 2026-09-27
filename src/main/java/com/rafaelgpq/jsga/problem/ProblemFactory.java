package com.rafaelgpq.jsga.problem;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Creates objective implementations named in the runtime configuration.
 */
public final class ProblemFactory {

    public static final String DEFAULT_PROBLEM = "com.rafaelgpq.jsga.problem.MaxOnesProblem";

    private ProblemFactory() {
    }

    public static Problem create(String className) {
        if (className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException("Problem class name must not be empty.");
        }
        try {
            Class<?> type = Class.forName(className.trim());
            if (!Problem.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Configured problem must implement " + Problem.class.getName()
                        + ": " + className);
            }
            Constructor<?> constructor = type.getDeclaredConstructor();
            Object instance = constructor.newInstance();
            return (Problem) instance;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalArgumentException("Unable to create configured problem '" + className + "'.", exception);
        }
    }
}
