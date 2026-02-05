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

    private UUID memberId;
    private UUID orderId;
    private String productName;
    private String cancelReason;
    private PaymentMethod method;
    private long amount;

    @Deprecated(forRemoval = true)
    public OrderCanceledEvent(UUID memberId, UUID orderId, String cancelReason, PaymentMethod method, long amount) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.cancelReason = cancelReason;
        this.method = method;
        this.amount = amount;
    }

    public OrderCanceledEvent(Clock clock,
                              UUID memberId,
                              UUID orderId,
                              String productName,
                              PaymentMethod method,
                              long amount,
                              String cancelReason) {
        super(clock);
        this.memberId = memberId;
        this.orderId = orderId;
        this.productName = productName;
        this.method = method;
        this.amount = amount;
        this.cancelReason = cancelReason;
    }

    public enum PaymentMethod {
        POINT,
        PG
    }
}
