package com.rafaelgpq.jsga.track.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DummyStatsTest {

    @Test
    void isAQuietNoOpStatisticsTracker() {
        DummyStats stats = new DummyStats();

        stats.incrementTrials();
        stats.updateBest(-12.0);
        stats.accumulateOnSum(3.0);
        stats.accumulateOffSum(4.0);
        stats.saveBest(null);
        stats.dumpCheckpoint();

        assertThat(stats.getBest()).isZero();
        assertThat(stats.shouldSaveBest()).isFalse();
        assertThat(stats.shouldDump()).isFalse();
    }
}
