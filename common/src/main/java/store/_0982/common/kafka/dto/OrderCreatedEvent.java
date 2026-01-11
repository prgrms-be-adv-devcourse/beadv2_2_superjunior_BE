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
public class OrderCreatedEvent extends BaseEvent {

    private UUID id;
    private UUID memberId;
    private String productName;

    public OrderCreatedEvent(Clock clock, UUID id, UUID memberId, String productName) {
        super(clock);
        this.id = id;
        this.memberId = memberId;
        this.productName = productName;
    }
}
