package store._0982.commerce.domain.order;

public class OrderCancellationPolicy {

    private static final double CANCELLATION_FEE_RATE = 0.20;   // 20%
    private static final long SHIPPING_FEE = 6000L;             // 택배비

    public static RefundAmount calculate(Order order, CancellationType type) {
        long totalAmount = order.getPrice() * order.getQuantity();

        return switch (type) {
            case BEFORE_GROUP_PURCHASE_SUCCESS ->
                    new RefundAmount(totalAmount, 0L);

            case WITHIN_48_HOURS ->
                    new RefundAmount(
                            (long) (totalAmount * (1 - CANCELLATION_FEE_RATE)),
                            (long) (totalAmount * CANCELLATION_FEE_RATE)
                    );

            case AFTER_48_HOURS ->
                    new RefundAmount(
                            (long) (totalAmount * (1 - CANCELLATION_FEE_RATE)) - SHIPPING_FEE,
                            (long) (totalAmount * CANCELLATION_FEE_RATE)
                    );
        };
    }

    public record RefundAmount(
            Long refundAmount,          // 환불 금액
            Long cancellationFee        // 취소 수수료
    ) {}

    public enum CancellationType {
        BEFORE_GROUP_PURCHASE_SUCCESS,  // 공구 성공 전
        WITHIN_48_HOURS,                // 48시간 이내
        AFTER_48_HOURS                  // 48시간 ~ 2주
    }
}
