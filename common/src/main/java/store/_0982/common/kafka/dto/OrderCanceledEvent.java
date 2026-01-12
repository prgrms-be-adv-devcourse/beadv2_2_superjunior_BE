package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unused", "java:S2094"})
public class OrderCanceledEvent extends BaseEvent {

    private UUID orderId;
    private PaymentMethod method;

    public OrderCanceledEvent(Clock clock, UUID orderId, PaymentMethod method) {
        super(clock);
        this.orderId = orderId;
        this.method = method;
    }

    public enum PaymentMethod {
        POINT,
        PG
    }
}
