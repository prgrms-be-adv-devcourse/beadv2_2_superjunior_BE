package store._0982.batch.batch.recommendation.processor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.ojalgo.array.Array1D;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VectorUtil {

    private static final int DIMENSION = 1536;

    public static float[] getAverageVector(List<float[]> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return new float[DIMENSION];
        }

        Array1D<Double> vectorSum = Array1D.R064.make(DIMENSION);

        int count = 0;
        for (float[] vector : vectors) {
            if (vector == null || vector.length != DIMENSION) {
                continue;
            }
            for (int i = 0; i < DIMENSION; i++) {
                vectorSum.set(i, vectorSum.doubleValue(i) + vector[i]);
            }
            count++;
        }

        if (count == 0) {
            return new float[DIMENSION];
        }

        float[] avgVector = new float[DIMENSION];
        for (int i = 0; i < DIMENSION; i++) {
            avgVector[i] = (float) (vectorSum.doubleValue(i) / count);
        }
        return avgVector;
    }

}
