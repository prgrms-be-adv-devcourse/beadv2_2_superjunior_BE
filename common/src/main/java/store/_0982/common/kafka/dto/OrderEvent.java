package store._0982.common.kafka.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class OrderEvent extends BaseEvent {
    private final UUID id;
    private final UUID memberId;
    private final String status;
    private final String productName;

    public OrderEvent(Clock clock, UUID id, UUID memberId, String status, String productName) {
        super(clock);
        this.id = id;
        this.memberId = memberId;
        this.status = status;
        this.productName = productName;
    }
}
