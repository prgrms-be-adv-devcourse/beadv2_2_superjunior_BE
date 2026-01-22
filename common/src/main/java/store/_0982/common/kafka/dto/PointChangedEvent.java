package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Deprecated(forRemoval = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class PointChangedEvent extends BaseEvent {
    private UUID id;
    private UUID memberId;
    private long amount;
    private Status status;
    private String paymentMethod;

    public PointChangedEvent(Clock clock, UUID id, UUID memberId, long amount, Status status, String paymentMethod) {
        super(clock);
        this.id = id;
        this.memberId = memberId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public enum Status {
        USED,       // 사용
        REFUNDED,   // 환불(반환)
        CHARGED,    // 충전
        WITHDRAWN   // 출금
    }
}
