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
     * 정책 타입 반환
     *
     * @return 정책 타입
     */
    PolicyType getPolicyType();

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
