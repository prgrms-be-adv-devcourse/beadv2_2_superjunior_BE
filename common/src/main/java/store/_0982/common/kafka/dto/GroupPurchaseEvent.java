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

    /**
     * @deprecated {@link Status} 값을 이용해 주세요.
     */
    @Deprecated(forRemoval = true)
    private String status;

    private Status groupPurchaseStatus;

    private String endDate;
    private String updatedAt;
    private Integer currentQuantity;
    private ProductEvent productEvent;
    private EventStatus kafkaStatus;

    // Product 정보
    private long originalPrice;
    private ProductCategory productCategory;

    @Deprecated(forRemoval = true)
    public GroupPurchaseEvent(UUID id, UUID sellerId, String title, String description, Long discountedPrice, String endDate, String updatedAt, Integer currentQuantity, ProductEvent productEvent, EventStatus kafkaStatus, String status) {
        this.id = id;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.discountedPrice = discountedPrice;
        this.endDate = endDate;
        this.updatedAt = updatedAt;
        this.currentQuantity = currentQuantity;
        this.productEvent = productEvent;
        this.kafkaStatus = kafkaStatus;
        this.status = status;
    }

    public GroupPurchaseEvent(UUID id, UUID sellerId, String title, String description, Long discountedPrice, Status groupPurchaseStatus, String endDate, String updatedAt, Integer currentQuantity, EventStatus kafkaStatus, long originalPrice, ProductCategory productCategory) {
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
        OPENED,
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
