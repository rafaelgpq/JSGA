package com.rafaelgpq.jsga.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock(Resources.SYSTEM_ERR)
class LogManagerTest {

    @AfterEach
    void resetLoggingState() {
        LogManager.configureFromProperties(new Properties());
    }

    @Test
    void writesEnabledMessagesToTheCorrectStreams() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        try (PrintStream captureOut = new PrintStream(output, true, StandardCharsets.UTF_8);
             PrintStream captureErr = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            System.setOut(captureOut);
            System.setErr(captureErr);
            LogManager.disableTrace();
            LogManager.setQuietMode(false);
            LogManager.info("ready");
            LogManager.warn("warning");
            LogManager.error("error");
            LogManager.trace("hidden");
            LogManager.enableTrace();
            LogManager.trace("details");
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("[INFO] ready", "[TRACE] details")
                .doesNotContain("hidden");
        assertThat(errors.toString(StandardCharsets.UTF_8))
                .contains("[WARNING] warning", "[ERROR] error");
    }

    @Test
    void quietModeSuppressesMessagesAndPropertiesConfigureBothFlags() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        try (PrintStream captureOut = new PrintStream(output, true, StandardCharsets.UTF_8);
             PrintStream captureErr = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            System.setOut(captureOut);
            System.setErr(captureErr);
            Properties properties = new Properties();
            properties.setProperty("trace", "true");
            properties.setProperty("quiet", "true");
            LogManager.configureFromProperties(properties);
            LogManager.info("hidden");
            LogManager.warn("hidden");
            LogManager.error("hidden");
            LogManager.trace("hidden");
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        assertThat(output.toByteArray()).isEmpty();
        assertThat(errors.toByteArray()).isEmpty();
    }

    @Test
    void fatalThrowsWithTheFatalPrefix() {
        assertThatThrownBy(() -> LogManager.fatal("broken"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("[FATAL] broken");
    }
}
