package store._0982.common.kafka.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class SettlementEvent extends BaseEvent {
    private final UUID id;
    private final UUID sellerId;
    private final OffsetDateTime start;
    private final OffsetDateTime end;
    private final String status;
    private final long totalAmount;
    private final BigDecimal serviceFee;
    private final BigDecimal settlementAmount;
}
