package com.rafaelgpq.jsga.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock("com.rafaelgpq.jsga.util.RandomUtils")
class EncodingUtilitiesTest {

    @Test
    void packsAndUnpacksBitsForFullAndPartialBytes() {
        boolean[] bits = {true, false, true, false, false, true, false, true, true, false, true};

        byte[] packed = EncodeUtils.pack(bits, 1, 3);

        assertThat(packed).containsExactly((byte) 0b10100101, (byte) 0b00000101);
        assertThat(DecodeUtils.unpack(packed, bits.length)).containsExactly(bits);
    }

    @Test
    void integerAndGrayConversionsUseConsistentLeastSignificantBitFirstOrder() {
        boolean[] binary = EncodeUtils.intToBinary(0b10110, 5);
        assertThat(binary).containsExactly(false, true, true, false, true);
        assertThat(EncodeUtils.binaryToInt(binary)).isEqualTo(0b10110);
        assertThat(EncodeUtils.grayToBinary(EncodeUtils.binaryToGray(binary))).containsExactly(binary);
    }

    @Test
    void aliasUsesTheSeededRandomGenerator() {
        RandomUtils.initialize(991L);
        double expected = 2.0 + RandomUtils.nextDouble();
        RandomUtils.initialize(991L);

        assertThat(DecodeUtils.binaryToDouble(new boolean[]{true, false}, true)).isEqualTo(expected);
    }

    @Test
    void randomSnapshotRestoresPrimitiveAndGaussianSequence() {
        RandomUtils.initialize(773L);
        RandomUtils.nextGaussian();
        long[] snapshot = RandomUtils.snapshot();
        int expectedInt = RandomUtils.nextInt(1_000_000);
        double expectedGaussian = RandomUtils.nextGaussian();
        double expectedDouble = RandomUtils.nextDouble();

        RandomUtils.restore(snapshot);

        assertThat(RandomUtils.nextInt(1_000_000)).isEqualTo(expectedInt);
        assertThat(RandomUtils.nextGaussian()).isEqualTo(expectedGaussian);
        assertThat(RandomUtils.nextDouble()).isEqualTo(expectedDouble);
    }

    @Test
    void rejectsInvalidBitShapesAndLengths() {
        assertThatThrownBy(() -> DecodeUtils.unpack(new byte[0], 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly");
        assertThatThrownBy(() -> DecodeUtils.unpack(null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not be null");
        assertThatThrownBy(() -> DecodeUtils.unpack(new byte[1], -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not be negative");
        assertThatThrownBy(() -> DecodeUtils.unpack(new byte[1], 1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("multiple of eight");
        assertThatThrownBy(() -> DecodeUtils.unpack(new byte[1], 0, 8))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Slop bit");
        assertThatThrownBy(() -> DecodeUtils.unpack(new byte[0], 8, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly");
        assertThatThrownBy(() -> DecodeUtils.degray(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not be null");
        assertThatThrownBy(() -> DecodeUtils.binaryToDouble(null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not be null");
        assertThatThrownBy(() -> EncodeUtils.pack(new boolean[]{true}, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EncodeUtils.intToBinary(1, 33))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EncodeUtils.binaryToInt(new boolean[33]))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
