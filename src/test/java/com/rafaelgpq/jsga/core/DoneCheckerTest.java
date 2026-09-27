package com.rafaelgpq.jsga.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoneCheckerTest {

    @Test
    void plateauCountsFromFirstObservedBestAndResetsOnMinimizingImprovement() {
        DoneChecker checker = new DoneChecker(2, 0, false);

        checker.update(-4.0, 1);
        assertThat(checker.getPlateauCounter()).isZero();
        assertThat(checker.isDone()).isFalse();

        checker.update(-4.0, 2);
        assertThat(checker.getPlateauCounter()).isEqualTo(1);
        assertThat(checker.isDone()).isFalse();

        checker.update(-5.0, 3);
        assertThat(checker.getPreviousBest()).isEqualTo(-5.0);
        assertThat(checker.getPlateauCounter()).isZero();

        checker.update(-5.0, 4);
        checker.update(-5.0, 5);
        assertThat(checker.isDone()).isTrue();
    }

    @Test
    void trialLimitTerminatesAtOrAboveLimitAndZeroDisablesIt() {
        DoneChecker limited = new DoneChecker(0, 3, false);
        limited.update(1.0, 2);
        assertThat(limited.isDone()).isFalse();
        limited.update(1.0, 3);
        assertThat(limited.isDone()).isTrue();

        DoneChecker unlimited = new DoneChecker(0, 0, false);
        unlimited.update(1.0, Long.MAX_VALUE);
        assertThat(unlimited.isDone()).isFalse();
    }

    @Test
    void lastFlagStopsAndInvalidInputsAreRejected() {
        DoneChecker immediate = new DoneChecker(0, 0, true);
        immediate.update(0.0, 0);
        assertThat(immediate.isDone()).isTrue();

        assertThatThrownBy(() -> new DoneChecker(-1, 0, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DoneChecker(0, -1, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> immediate.update(Double.NaN, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> immediate.update(0.0, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
