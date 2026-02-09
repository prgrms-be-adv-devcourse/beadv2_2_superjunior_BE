package store._0982.batch.batch.recommendation.processor;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VectorUtilTest {

    private static final int DIMENSION = 1536;

    @Test
    void returnsZeroVectorWhenInputIsNull() {
        float[] result = VectorUtil.getAverageVector(null);

        assertEquals(DIMENSION, result.length);
        assertArrayEquals(new float[DIMENSION], result, 0.0f);
    }

    @Test
    void returnsZeroVectorWhenInputIsEmpty() {
        float[] result = VectorUtil.getAverageVector(List.of());

        assertEquals(DIMENSION, result.length);
        assertArrayEquals(new float[DIMENSION], result, 0.0f);
    }

    @Test
    void ignoresInvalidVectorsAndReturnsZeroWhenNoneValid() {
        float[] invalid = new float[10];

        float[] result = VectorUtil.getAverageVector(Arrays.asList(null, invalid));

        assertEquals(DIMENSION, result.length);
        assertArrayEquals(new float[DIMENSION], result, 0.0f);
    }

    @Test
    void averagesOnlyValidVectors() {
        float[] v1 = filledVector(1.0f);
        float[] v2 = filledVector(3.0f);
        float[] invalid = new float[5];

        float[] result = VectorUtil.getAverageVector(List.of(v1, invalid, v2));

        assertEquals(DIMENSION, result.length);
        assertEquals(2.0f, result[0], 0.0001f);
        assertEquals(2.0f, result[DIMENSION - 1], 0.0001f);
    }

    private static float[] filledVector(float value) {
        float[] vector = new float[DIMENSION];
        Arrays.fill(vector, value);
        return vector;
    }
}
