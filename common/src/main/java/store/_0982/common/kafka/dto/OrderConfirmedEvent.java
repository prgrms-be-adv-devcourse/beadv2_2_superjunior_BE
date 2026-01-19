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
public class OrderConfirmedEvent extends BaseEvent {

    private UUID orderId;
    private UUID memberId;
    private String productName;

    public OrderConfirmedEvent(Clock clock, UUID orderId, UUID memberId, String productName) {
        super(clock);
        this.orderId = orderId;
        this.memberId = memberId;
        this.productName = productName;
    }
}
