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
    private long amount;
    private UUID paymentId;
    private Status status;

    @Deprecated(forRemoval = true)
    public PaymentChangedEvent(UUID memberId, UUID orderId, Status status) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.status = status;
    }

    public PaymentChangedEvent(Clock clock, UUID memberId, UUID orderId, long amount, UUID paymentId, Status status) {
        super(clock);
        this.memberId = memberId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentId = paymentId;
        this.status = status;
    }

    public enum Status {
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        REFUNDED
    }
}
