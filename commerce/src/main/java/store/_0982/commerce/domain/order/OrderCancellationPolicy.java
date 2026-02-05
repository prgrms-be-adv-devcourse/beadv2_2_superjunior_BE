package store._0982.commerce.domain.order;

public interface OrderCancellationPolicy {

    /**
     * 환불 금액 계산
     *
     * @param order 취소할 주문
     * @return 환불 금액과 취소 수수료
     */
    RefundAmount calculate(Order order);

    /**
     * 정책 식별자 (버전 포함)
     */
    String getPolicyId();

    /**
     * 정책 타입 반환
     */
    PolicyType getPolicyType();

    /**
     * 정책 스냅샷(JSON 등)을 생성한다.
     */
    default String buildSnapshot(RefundAmount refundAmount) {
        return String.format(
                "{\"policyType\":\"%s\",\"policyId\":\"%s\",\"cancellationFee\":%d,\"shippingFee\":%d,\"refundAmount\":%d}",
                getPolicyType().name(),
                getPolicyId(),
                refundAmount.cancellationFee(),
                refundAmount.shippingFee(),
                refundAmount.refundAmount()
        );
    }

    /**
     * 환불 금액 정보
     *
     * @param refundAmount 고객에게 환불할 금액
     * @param cancellationFee 취소 수수료 (판매자에게 전달)
     */
    record RefundAmount(
            long refundAmount,
            long cancellationFee,
            long shippingFee
    ) {}

    enum PolicyType {
        VOID,
        REVERSAL,
        REFUND
    }
}
