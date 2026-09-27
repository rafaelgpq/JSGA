package com.rafaelgpq.jsga.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlagConfiguratorTest {

    @Test
    void requiresNonEmptyConfigurationPathAndExposesLegacyFlags() throws IOException {
        assertThatThrownBy(() -> new FlagConfigurator(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("path must not be empty");

        FlagConfigurator config = configFile("trace=true\nlog=true\nbest=false\noptions=Ax\n");
        assertThat(config.isTraceEnabled()).isTrue();
        assertThat(config.isLogEnabled()).isTrue();
        assertThat(config.isBestEnabled()).isFalse();
        assertThat(config.getOptions()).isEqualTo("Ax");
        FlagConfigurator defaults = configFile("");
        assertThat(defaults.isLogEnabled()).isFalse();
        assertThat(defaults.isBestEnabled()).isTrue();
    }

    @Test
    void validateAcceptsDefaultsAndTrimsValues() throws IOException {
        FlagConfigurator config = configFile("population.size = 4\n"
                + "crossover.type = ONEPOINT\n");

        config.validate();

        assertThat(config.getInt("population.size", 1)).isEqualTo(4);
        assertThat(config.getString("crossover.type", "twopoint")).isEqualTo("ONEPOINT");
    }

    @Test
    void validateRejectsInvalidRatesAndUnsupportedOperators() throws IOException {
        FlagConfigurator invalidRate = configFile("mutation.rate=1.01\n");
        assertThatThrownBy(invalidRate::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mutation.rate");

        FlagConfigurator invalidCrossover = configFile("crossover.type=pmx\n");
        assertThatThrownBy(invalidCrossover::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("crossover.type");
    }

    @Test
    void typedGettersRejectMalformedValues() throws IOException {
        FlagConfigurator invalidBoolean = configFile("elitism.enabled=perhaps\n");
        assertThatThrownBy(() -> invalidBoolean.getBoolean("elitism.enabled", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expected true or false");

        FlagConfigurator invalidNumber = configFile("population.size=ten\n");
        assertThatThrownBy(() -> invalidNumber.getInt("population.size", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("population.size");
    }

    @Test
    void typedGettersParseLongAndFiniteDoubleAndRejectNonFiniteValues() throws IOException {
        FlagConfigurator config = configFile("seed=9223372036854775000\n"
                + "rate=0.25\n");
        assertThat(config.getLong("seed", 0L)).isEqualTo(9223372036854775000L);
        assertThat(config.getDouble("rate", 0.0)).isEqualTo(0.25);

        FlagConfigurator notANumber = configFile("rate=NaN\n");
        assertThatThrownBy(() -> notANumber.getDouble("rate", 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        FlagConfigurator infinity = configFile("rate=Infinity\n");
        assertThatThrownBy(() -> infinity.getDouble("rate", 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");

        FlagConfigurator invalidLong = configFile("seed=not-a-long\n");
        assertThatThrownBy(() -> invalidLong.getLong("seed", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("seed");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "population.size=0",
            "gene.length=0",
            "max.generations=-1",
            "crossover.rate=-0.01",
            "mutation.rate=1.01",
            "selection.type=roulettee",
            "selection.parameter=0",
            "selection.parameter=1.01",
            "gap.size=-0.01",
            "gap.size=1.01",
            "mutation.type=uniform",
            "initialization.type=seeded",
            "elitism.enabled=yes",
            "random_immigrants.count=-1",
            "crowding.similarity.threshold=1.1",
            "fitness_sharing.niche_radius=0",
            "island.count=0",
            "island.migration.interval=0",
            "island.migration.size=-1",
            "convergence.few.threshold=-1",
            "convergence.min_generations=-1",
            "convergence.threshold=-0.1",
            "convergence.maxconv=-1",
            "maxbias=1.1",
            "sigma.factor=-1",
            "termination.trial_limit=-1",
            "termination.stagnation.generations=-1"
    })
    void validateRejectsOutOfRangeAndUnsupportedSettings(String setting) throws IOException {
        FlagConfigurator config = configFile(setting + "\n");

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateRequiresFitnessSharingToggleForItsDiversityStrategy() throws IOException {
        FlagConfigurator invalidSharing = configFile("diversity.enabled=true\n"
                + "diversity.strategy=fitness_sharing\n");

        assertThatThrownBy(invalidSharing::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fitness_sharing.enabled");
    }

    @Test
    void validateRequiresDiversityStrategyWhenDiversityIsEnabled() throws IOException {
        FlagConfigurator config = configFile("diversity.enabled=true\n");

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("diversity.strategy");
    }

    @Test
    void validateChecksDpeParameterSegmentsAndScales() throws IOException {
        FlagConfigurator validDpe = configFile("population.size=4\n"
                + "gene.length=8\n"
                + "dpe.enabled=true\n"
                + "dpe.positions=4,8\n"
                + "dpe.factors=0.1,0.1\n"
                + "dpe.bases=-1,0\n");
        validDpe.validate();

        FlagConfigurator invalidDpe = configFile("gene.length=8\n"
                + "dpe.enabled=true\n"
                + "dpe.positions=4,9\n"
                + "dpe.factors=0.1,0.1\n"
                + "dpe.bases=-1,0\n");
        assertThatThrownBy(invalidDpe::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gene.length");
    }

    @Test
    void restartRejectsStatefulTerminationHistory() throws IOException {
        FlagConfigurator stagnationRestart = configFile("restart.enabled=true\n"
                + "restart.file=checkpoint.txt\n"
                + "termination.stagnation.generations=3\n");
        assertThatThrownBy(stagnationRestart::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stagnation termination");

        FlagConfigurator convergenceRestart = configFile("restart.enabled=true\n"
                + "restart.file=checkpoint.txt\n"
                + "convergence.enabled=true\n"
                + "convergence.maxconv=2\n");
        assertThatThrownBy(convergenceRestart::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("convergence stagnation");
    }

    @Test
    void validateChecksManualInitializationGenes() throws IOException {
        FlagConfigurator validGenes = configFile("gene.length=4\n"
                + "initialization.type=manual\n"
                + "initialization.genes=0101, 1010\n");
        validGenes.validate();

        FlagConfigurator invalidGenes = configFile("gene.length=4\n"
                + "initialization.type=manual\n"
                + "initialization.genes=010,1010\n");
        assertThatThrownBy(invalidGenes::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("initialization.genes")
                .hasMessageContaining("length 4");
    }

    @Test
    void validateRequiresRestartCheckpointAndValidSchemaOutput() throws IOException {
        FlagConfigurator missingCheckpoint = configFile("restart.enabled=true\n");
        assertThatThrownBy(missingCheckpoint::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("restart.file");

        FlagConfigurator restartWithDpe = configFile("restart.enabled=true\nrestart.file=state\n"
                + "dpe.enabled=true\ndpe.positions=4,8\ndpe.factors=1,1\ndpe.bases=0,0\n");
        assertThatThrownBy(restartWithDpe::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DPE");

        FlagConfigurator restartWithSchema = configFile("restart.enabled=true\nrestart.file=state\n"
                + "schema.enabled=true\nschema.file=schema\n");
        assertThatThrownBy(restartWithSchema::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schema tracking");

        FlagConfigurator missingSchema = configFile("schema.enabled=true\n");
        assertThatThrownBy(missingSchema::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schema.file");

        FlagConfigurator schemaOverwrite = configFile("schema.enabled=true\nschema.file=same\n"
                + "schema.output=./same\n");
        assertThatThrownBy(schemaOverwrite::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overwrite");
    }

    @Test
    void validateChecksRequiredCheckpointDpeAndProblemOptions() throws IOException {
        FlagConfigurator missingCheckpointPath = configFile("checkpoint.enabled=true\n");
        assertThatThrownBy(missingCheckpointPath::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checkpoint.file");

        FlagConfigurator missingDpeLog = configFile("population.size=4\ngene.length=8\n"
                + "dpe.enabled=true\ndpe.positions=4,8\ndpe.factors=1,1\ndpe.bases=0,0\ndpe.log= \n");
        assertThatThrownBy(missingDpeLog::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dpe.log");

        FlagConfigurator emptyProblem = configFile("problem= \n");
        assertThatThrownBy(emptyProblem::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("problem");
    }

    @Test
    void validateChecksReportOutputSettingsOnlyWhenReportingIsEnabled() throws IOException {
        FlagConfigurator reportDisabled = configFile("report.enabled=false\nreport.file= \n");
        reportDisabled.validate();

        FlagConfigurator missingReportFile = configFile("report.enabled=true\nreport.file= \n");
        assertThatThrownBy(missingReportFile::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("report.file");

        FlagConfigurator invalidInterval = configFile("report.enabled=true\nreport.interval=0\n");
        assertThatThrownBy(invalidInterval::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("report.interval");

        FlagConfigurator negativeInterval = configFile("report.enabled=true\nreport.interval=-1\n");
        assertThatThrownBy(negativeInterval::validate).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("report.interval");

        FlagConfigurator validReport = configFile("report.enabled=true\nreport.file=out.txt\nreport.interval=5\n");
        validReport.validate();
    }

    @Test
    void validateRejectsMalformedManualGenomeLists() throws IOException {
        FlagConfigurator emptyEntry = configFile("gene.length=4\n"
                + "initialization.type=manual\n"
                + "initialization.genes=0101,\n");
        assertThatThrownBy(emptyEntry::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("initialization.genes");

        FlagConfigurator badCharacter = configFile("gene.length=4\n"
                + "initialization.type=manual\n"
                + "initialization.genes=01x1\n");
        assertThatThrownBy(badCharacter::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("binary");

        FlagConfigurator manualMissingGenes = configFile("initialization.type=manual\n");
        assertThatThrownBy(manualMissingGenes::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("initialization.genes");
    }

    private FlagConfigurator configFile(String contents) throws IOException {
        Path path = Files.createTempFile("jsga-config", ".properties");
        path.toFile().deleteOnExit();
        Files.writeString(path, contents);
        return new FlagConfigurator(path.toString());
    }
}
