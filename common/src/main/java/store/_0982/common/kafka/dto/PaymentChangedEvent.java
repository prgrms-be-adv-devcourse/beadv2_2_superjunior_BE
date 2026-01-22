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
public class PaymentChangedEvent extends BaseEvent {

    private UUID memberId;
    private UUID orderId;
    private PaymentMethod method;
    private Status status;

    public PaymentChangedEvent(Clock clock, UUID memberId, UUID orderId, PaymentMethod method, Status status) {
        super(clock);
        this.memberId = memberId;
        this.orderId = orderId;
        this.method = method;
        this.status = status;
    }

    public enum Status {
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        REFUNDED
    }

    public enum PaymentMethod {
        POINT,
        PG
    }
}
