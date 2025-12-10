package store._0982.order.domain;

public enum OrderStatus {
    SCHEDULED,      // 시작전
    IN_PROGRESS,    // 진행중
    SUCCESS,        // 완료- 성공
    FAILED,         // 완료 - 실패
}
