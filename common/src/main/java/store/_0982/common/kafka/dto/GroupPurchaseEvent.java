package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unused", "java:S107"})
public class GroupPurchaseEvent extends BaseEvent {
    private UUID id;
    private UUID sellerId;
    private String title;
    private String description;
    private Long discountedPrice;
    private UUID productId;

    private Status groupPurchaseStatus;

    private String endDate;
    private String updatedAt;
    private Integer currentQuantity;
    private EventStatus kafkaStatus;

    // Product 정보
    private long originalPrice;
    private ProductCategory productCategory;

    public GroupPurchaseEvent(Clock clock,
                              UUID id,
                              UUID sellerId,
                              String title,
                              String description,
                              Long discountedPrice,
                              Status groupPurchaseStatus,
                              String endDate,
                              String updatedAt,
                              Integer currentQuantity,
                              EventStatus kafkaStatus,
                              long originalPrice,
                              ProductCategory productCategory) {
        super(clock);
        this.id = id;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.discountedPrice = discountedPrice;
        this.groupPurchaseStatus = groupPurchaseStatus;
        this.endDate = endDate;
        this.updatedAt = updatedAt;
        this.currentQuantity = currentQuantity;
        this.kafkaStatus = kafkaStatus;
        this.originalPrice = originalPrice;
        this.productCategory = productCategory;
    }

    public enum Status {
        SCHEDULED,
        OPEN,
        SUCCESS,
        FAILED
    }

    public enum ProductCategory {
        HOME,
        FOOD,
        HEALTH,
        BEAUTY,
        FASHION,
        ELECTRONICS,
        KIDS,
        HOBBY,
        PET
    }

    public enum EventStatus {
        DELETE_GROUP_PURCHASE,
        INCREASE_PARTICIPATE,
        UPDATE_GROUP_PURCHASE,
        CREATE_GROUP_PURCHASE
    }
}
