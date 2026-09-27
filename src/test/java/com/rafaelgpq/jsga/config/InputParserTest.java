package com.rafaelgpq.jsga.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputParserTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsAndReadsTypedPropertiesAndExposesLoadedProperties() throws Exception {
        Path config = tempDir.resolve("input.properties");
        Files.writeString(config, "name=JSGA\ncount=17\nrate=0.25\nenabled=true\n");
        InputParser parser = new InputParser();

        parser.load(config.toString());

        assertThat(parser.get("name")).isEqualTo("JSGA");
        assertThat(parser.get("missing")).isNull();
        assertThat(parser.getOrDefault("missing", "default")).isEqualTo("default");
        assertThat(parser.getInt("count", 1)).isEqualTo(17);
        assertThat(parser.getInt("missing", 9)).isEqualTo(9);
        assertThat(parser.getDouble("rate", 1.0)).isEqualTo(0.25);
        assertThat(parser.getDouble("missing", 0.5)).isEqualTo(0.5);
        assertThat(parser.getBoolean("enabled", false)).isTrue();
        assertThat(parser.getBoolean("missing", true)).isTrue();
        parser.getProperties().setProperty("new", "value");
        assertThat(parser.get("new")).isEqualTo("value");
    }

    @Test
    void propagatesMissingFileAndMalformedNumericValues() throws Exception {
        InputParser parser = new InputParser();
        assertThatThrownBy(() -> parser.load(tempDir.resolve("missing.properties").toString()))
                .isInstanceOf(java.io.IOException.class);

        Path config = tempDir.resolve("malformed.properties");
        Files.writeString(config, "count=NaN\nrate=bad\n");
        parser.load(config.toString());
        assertThatThrownBy(() -> parser.getInt("count", 0))
                .isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> parser.getDouble("rate", 0.0))
                .isInstanceOf(NumberFormatException.class);
    }
}
