package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unused", "java:S107"})
public class GroupPurchaseEvent extends BaseEvent {
    private UUID id;
    private Integer minQuantity;
    private Integer maxQuantity;
    private String title;
    private String description;
    private Integer discountedPrice;
    private String status;
    private String sellerName;
    private String productName;
    private String startDate;
    private String endDate;
    private String createdAt;
    private String updatedAt;
    private Integer currentQuantity;
    private String kafkaStatus;

    public GroupPurchaseEvent(Clock clock, UUID id, Integer minQuantity, Integer maxQuantity, String title, String description, Integer discountedPrice, String status, String sellerName, String productName, String startDate, String endDate, String createdAt, String updatedAt, Integer currentQuantity, String kafkaStatus) {
        super(clock);
        this.id = id;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.title = title;
        this.description = description;
        this.discountedPrice = discountedPrice;
        this.status = status;
        this.sellerName = sellerName;
        this.productName = productName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.currentQuantity = currentQuantity;
        this.kafkaStatus = kafkaStatus;
    }
}


