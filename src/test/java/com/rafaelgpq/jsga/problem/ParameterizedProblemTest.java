package com.rafaelgpq.jsga.problem;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParameterizedProblemTest {

    @Test
    void defaultPackedGenomeEvaluationExplainsThatDpeDecodingIsRequired() {
        ParameterizedProblem problem = parameters -> parameters.length;

        assertThatThrownBy(() -> problem.evaluate(new byte[]{1}, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires DPE parameter decoding");
    }
}
