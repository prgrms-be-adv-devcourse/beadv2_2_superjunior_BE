package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class PointChangedEvent extends BaseEvent {

    private UUID orderId;
    private UUID memberId;
    private long amount;
    private UUID transactionId;
    private Status status;

    public PointChangedEvent(Clock clock, UUID orderId, UUID memberId, long amount, UUID transactionId, Status status) {
        super(clock);
        this.orderId = orderId;
        this.memberId = memberId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.status = status;
    }

    public enum Status {
        USED,       // 사용
        REFUNDED,   // 환불(반환)
        CHARGED,    // 충전
        WITHDRAWN   // 출금
    }
}
