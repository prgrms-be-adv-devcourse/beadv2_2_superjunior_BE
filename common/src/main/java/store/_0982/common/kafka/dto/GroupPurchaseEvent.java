package store._0982.common.kafka.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class GroupPurchaseEvent extends BaseEvent {
    private final UUID id;
    private final int minQuantity;
    private final Integer maxQuantity;
    private final String title;
    private final String description;
    private final int discountedPrice;
    private final String status;
    private final OffsetDateTime startDate;
    private final OffsetDateTime endDate;

    public GroupPurchaseEvent(Clock clock, UUID id, int minQuantity, Integer maxQuantity, String title, String description,
                              int discountedPrice, String status, OffsetDateTime startDate, OffsetDateTime endDate) {
        super(clock);
        this.id = id;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.title = title;
        this.description = description;
        this.discountedPrice = discountedPrice;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
