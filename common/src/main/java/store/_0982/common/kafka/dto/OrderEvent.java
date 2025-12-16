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
public class OrderEvent extends BaseEvent {
    private UUID id;
    private UUID memberId;
    private Status status;
    private String productName;

    public OrderEvent(Clock clock, UUID id, UUID memberId, Status status, String productName) {
        super(clock);
        this.id = id;
        this.memberId = memberId;
        this.status = status;
        this.productName = productName;
    }

    public enum Status {
        CREATED,
        SUCCESS,
        FAILED
    }
}
