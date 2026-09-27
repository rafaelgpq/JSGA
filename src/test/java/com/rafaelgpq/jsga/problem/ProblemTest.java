package com.rafaelgpq.jsga.problem;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProblemTest {

    @Test
    void maxOnesEvaluatesOnlyLogicalBits() {
        assertThat(new MaxOnesProblem().evaluate(new byte[]{(byte) 0xff}, 3)).isEqualTo(-3.0);
    }

    @Test
    void sphereProblemMinimizesDecodedParameterVectors() {
        SphereProblem sphere = new SphereProblem();
        assertThat(sphere.evaluateParameters(new double[]{-3.0, 4.0})).isEqualTo(25.0);
        assertThatThrownBy(() -> sphere.evaluateParameters(new double[]{Double.NaN}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void factoryCreatesConfiguredProblemsAndRejectsInvalidTypes() {
        assertThat(ProblemFactory.create(ProblemFactory.DEFAULT_PROBLEM)).isInstanceOf(MaxOnesProblem.class);
        assertThatThrownBy(() -> ProblemFactory.create("java.lang.String"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("implement");
        assertThatThrownBy(() -> ProblemFactory.create("no.such.Problem"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to create");
    }
}
