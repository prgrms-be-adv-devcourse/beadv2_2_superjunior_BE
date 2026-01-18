package store._0982.batch.domain.order;

public enum OrderStatus {
    PENDING,                // 주문 생성 (결제 대기)
  
    PAYMENT_COMPLETED,      // 결제 완료
    ORDER_FAILED,           // 주문 실패
    PAYMENT_FAILED,         // 결제 실패
    CANCELLED,              // 주문 취소
  
    GROUP_PURCHASE_SUCCESS, // 공동 구매 성공
    GROUP_PURCHASE_FAIL,    // 공동 구매 실패

    CANCEL_REQUESTED,       // 취소 요청
    CANCELLED,              // 취소 완료

    REVERSED_REQUESTED,     // 번복 요청
    REVERSED,               // 번복 완료

    RETURNED_REQUESTED,       // 반품 요청
    RETURNED                // 반품 완료
}
