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
public class OrderChangedEvent extends BaseEvent {
    private UUID id;
    private UUID memberId;
    private Status status;
    private String productName;

    public OrderChangedEvent(Clock clock, UUID id, UUID memberId, Status status, String productName) {
        super(clock);
        this.id = id;
        this.memberId = memberId;
        this.status = status;
        this.productName = productName;
    }

    public enum Status {
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        ORDER_FAILED,
        GROUP_PURCHASE_SUCCESS,
        GROUP_PURCHASE_FAIL
    }
}
