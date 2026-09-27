package com.rafaelgpq.jsga.dpe;

import com.rafaelgpq.jsga.config.FlagConfigurator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DPEConfigurationTest {

    @TempDir
    Path tempDir;

    @Test
    void createsValidatedEngineFromDisabledAndEnabledSegments() throws Exception {
        DPEConfiguration configuration = DPEConfiguration.from(config(
                "dpe.positions=-4,8\n"
                        + "dpe.factors=0.5,1.25\n"
                        + "dpe.bases=-2,3\n"), 8);

        DPEngine engine = configuration.createEngine(5, 2, 4, tempDir.resolve("dpe.log").toString());

        assertThat(engine.decodeParameters(new byte[]{0b00000000}, 8, false))
                .containsExactly(-2.0, 3.0);
        assertThatThrownBy(() -> configuration.createEngine(0, 2, 4, "log"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMissingMismatchedAndMalformedArrays() throws Exception {
        assertInvalid("", 8, "required");
        assertInvalid("dpe.positions=4\ndpe.factors=1,2\ndpe.bases=0\n", 8, "same non-zero");
        assertInvalid("dpe.positions=bad\ndpe.factors=1\ndpe.bases=0\n", 8, "integer");
        assertInvalid("dpe.positions=4\ndpe.factors=bad\ndpe.bases=0\n", 8, "Invalid number");
        assertInvalid("dpe.positions=4\ndpe.factors=NaN\ndpe.bases=0\n", 8, "must be finite");
        assertInvalid("dpe.positions=4\ndpe.factors=1\ndpe.bases=Infinity\n", 8, "must be finite");
    }

    @Test
    void rejectsNonIncreasingOutOfRangeAndTooNarrowSegments() throws Exception {
        assertInvalid("dpe.positions=4,4\ndpe.factors=1,1\ndpe.bases=0,0\n", 8, "must increase");
        assertInvalid("dpe.positions=4,9\ndpe.factors=1,1\ndpe.bases=0,0\n", 8, "fit within");
        assertInvalid("dpe.positions=1\ndpe.factors=1\ndpe.bases=0\n", 8, "at least two bits");
        assertInvalid("dpe.positions=4\ndpe.factors=0\ndpe.bases=0\n", 8, "greater than zero");
        assertInvalid("dpe.positions=4\ndpe.factors=-1\ndpe.bases=0\n", 8, "greater than zero");
    }

    private void assertInvalid(String settings, int geneLength, String message) throws Exception {
        assertThatThrownBy(() -> DPEConfiguration.from(config(settings), geneLength))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(message);
    }

    private FlagConfigurator config(String settings) throws Exception {
        Path path = tempDir.resolve("dpe-" + Math.abs(settings.hashCode()) + ".properties");
        Files.writeString(path, settings);
        return new FlagConfigurator(path.toString());
    }
}
