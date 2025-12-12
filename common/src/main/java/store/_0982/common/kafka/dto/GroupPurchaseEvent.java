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
    private Long discountedPrice;
    private String status;
    private String sellerName;
    private String startDate;
    private String endDate;
    private String createdAt;
    private String updatedAt;
    private Integer currentQuantity;
    private ProductEvent productEvent;
    private SearchKafkaStatus kafkaStatus;

    public GroupPurchaseEvent(Clock clock, UUID id, Integer minQuantity, Integer maxQuantity, String title, String description, Integer discountedPrice, String status, String sellerName, String startDate, String endDate, String createdAt, String updatedAt, Integer currentQuantity,ProductEvent productEvent, SearchKafkaStatus kafkaStatus) {
        super(clock);
        this.id = id;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.title = title;
        this.description = description;
        this.discountedPrice = (long) discountedPrice;
        this.status = status;
        this.sellerName = sellerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.currentQuantity = currentQuantity;
        this.productEvent = productEvent;
        this.kafkaStatus = kafkaStatus;
    }

    public enum SearchKafkaStatus {
        DELETE_GROUP_PURCHASE,
        INCREASE_PARTICIPATE,
        UPDATE_GROUP_PURCHASE,
        CREATE_GROUP_PURCHASE
    }
}


