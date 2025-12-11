package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unused", "java:S107"})
public class GroupPurchaseEvent extends BaseEvent {
    private UUID id;
    private int minQuantity;
    private Integer maxQuantity;
    private String title;
    private String description;
    private long discountedPrice;
    private String status;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    
    public GroupPurchaseEvent(
            UUID id,
            int minQuantity,
            Integer maxQuantity,
            String title,
            String description,
            long discountedPrice,
            String status,
            String startDate,
            String endDate
    ) {
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
