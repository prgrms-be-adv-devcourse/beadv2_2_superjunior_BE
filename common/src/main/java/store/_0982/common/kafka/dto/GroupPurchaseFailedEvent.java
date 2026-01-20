package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupPurchaseFailedEvent extends BaseEvent {

    private UUID groupPurchaseId;
    private String failReason;

    public GroupPurchaseFailedEvent(Clock clock, UUID groupPurchaseId, String failReason) {
        super(clock);
        this.groupPurchaseId = groupPurchaseId;
        this.failReason = failReason;
    }
}
