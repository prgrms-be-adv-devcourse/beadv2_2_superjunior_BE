package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

/**
 * @deprecated {@link GroupPurchaseEvent}로 대체합니다.
 */
@Deprecated(forRemoval = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class GroupPurchaseChangedEvent extends BaseEvent {
    private UUID id;
    private UUID sellerId;
    private String title;
    private Status status;
    private Long totalAmount;

    public GroupPurchaseChangedEvent(Clock clock, UUID id, UUID sellerId, String title, Status status, Long totalAmount) {
        super(clock);
        this.id = id;
        this.sellerId = sellerId;
        this.title = title;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public enum Status {
        SUCCESS,
        FAILED
    }
}
