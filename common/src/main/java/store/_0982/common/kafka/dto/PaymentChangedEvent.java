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
    private Status status;

    public PaymentChangedEvent(Clock clock, UUID memberId, UUID orderId, Status status) {
        super(clock);
        this.memberId = memberId;
        this.orderId = orderId;
        this.status = status;
    }

    public enum Status {
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        REFUNDED
    }
}
