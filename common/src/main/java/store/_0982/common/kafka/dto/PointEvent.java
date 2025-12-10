package store._0982.common.kafka.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PointEvent extends BaseEvent {
    private final UUID id;
    private final UUID memberId;
    private final long amount;
    private final Status status;
    private final String paymentMethod;

    public PointEvent(Clock clock, UUID id, UUID memberId, long amount, Status status, String paymentMethod) {
        super(clock);
        this.id = id;
        this.memberId = memberId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public enum Status {
        DEDUCTED,
        RETURNED,
        RECHARGED
    }
}
