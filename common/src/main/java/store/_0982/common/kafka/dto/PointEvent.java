package store._0982.common.kafka.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PointEvent extends BaseEvent {
    private final UUID id;
    private final UUID memberId;
    private final long amount;
    private final String status;
    private final String paymentMethod;
    private final OffsetDateTime requestedAt;
    private final OffsetDateTime approvedAt;

    public PointEvent(Clock clock, UUID id, UUID memberId, long amount, String status, String paymentMethod,
                      OffsetDateTime requestedAt, OffsetDateTime approvedAt) {
        super(clock);
        this.id = id;
        this.memberId = memberId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
    }
}
