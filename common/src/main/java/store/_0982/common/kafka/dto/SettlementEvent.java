package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unused", "java:S107"})
public class SettlementEvent extends BaseEvent {
    private UUID id;
    private UUID sellerId;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private Status status;
    private long totalAmount;
    private BigDecimal serviceFee;
    private BigDecimal settlementAmount;

    public SettlementEvent(Clock clock, UUID id, UUID sellerId, OffsetDateTime start, OffsetDateTime end,
                           Status status, long totalAmount, BigDecimal serviceFee, BigDecimal settlementAmount) {
        super(clock);
        this.id = id;
        this.sellerId = sellerId;
        this.start = start;
        this.end = end;
        this.status = status;
        this.totalAmount = totalAmount;
        this.serviceFee = serviceFee;
        this.settlementAmount = settlementAmount;
    }

    public enum Status {
        SUCCESS,
        FAILED
    }
}
