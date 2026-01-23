package store._0982.batch.domain.ai;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VectorUtil {

    private static final float CART_WEIGHT = 4f;
    private static final float ORDER_WEIGHT = 6f;
    private static final int DIMENSION = 1536;

    public static float[] getAverageVector(List<CartVector> cartVectors, List<OrderVector> orderVectors) {

        float weightSum = 0f;
        weightSum += cartVectors.size() * CART_WEIGHT;
        weightSum += orderVectors.size() * ORDER_WEIGHT;
        if (weightSum == 0f) {
            return new float[DIMENSION];
        }

        float[][] cartVectorArray = cartVectors.stream().map(cart->cart.getVector()).toArray(float[][]::new);
        float[][] orderVectorArray = orderVectors.stream().map(order->order.getVector()).toArray(float[][]::new);

        INDArray vectorSum = Nd4j.create(new float[DIMENSION]);

        for (float[] cartVector : cartVectorArray) {
            vectorSum.addi(Nd4j.create(cartVector).mul(CART_WEIGHT));
        }

        for (float[] orderVector : orderVectorArray) {
            vectorSum.addi(Nd4j.create(orderVector).mul(ORDER_WEIGHT));
        }

        INDArray avgVector = vectorSum.div(weightSum);
        return avgVector.toFloatVector();
    }

}
