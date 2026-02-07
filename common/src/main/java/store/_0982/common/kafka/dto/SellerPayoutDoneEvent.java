package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unused", "java:S107"})
public class SellerPayoutDoneEvent extends BaseEvent {
    private UUID id;
    private UUID sellerId;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private Status status;
    private long totalAmount;

    public SellerPayoutDoneEvent(Clock clock,
                                 UUID id,
                                 UUID sellerId,
                                 OffsetDateTime start,
                                 OffsetDateTime end,
                                 Status status,
                                 long totalAmount) {
        super(clock);
        this.id = id;
        this.sellerId = sellerId;
        this.start = start;
        this.end = end;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public enum Status {
        COMPLETED,
        FAILED,
        DEFERRED
    }
}
