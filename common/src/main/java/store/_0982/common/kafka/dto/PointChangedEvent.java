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
    private Status status;

    public PointChangedEvent(Clock clock, UUID orderId, UUID memberId, long amount, Status status) {
        super(clock);
        this.orderId = orderId;
        this.memberId = memberId;
        this.amount = amount;
        this.status = status;
    }

    public enum Status {
        DEDUCTED,
        RETURNED,
        CHARGED
    }
}
