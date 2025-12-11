package store._0982.common.kafka.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unused", "java:S107"})
public class GroupPurchaseSearchEvent extends BaseEvent {
    private UUID id;
    private Integer minQuantity;
    private Integer maxQuantity;
    private String title;
    private String description;
    private Long discountedPrice;
    private String status;
    private String sellerName;
    private String productName;
    private String startDate;
    private String endDate;
    private String createdAt;
    private String updatedAt;
    private Integer currentQuantity;
    private String kafkaStatus;

    public GroupPurchaseSearchEvent(
            UUID id,
            int minQuantity,
            int maxQuantity,
            String title,
            String description,
            long discountedPrice,
            String status,
            String sellerName,
            String productName,
            String startDate,
            String endDate,
            String createdAt,
            String updatedAt,
            int currentQuantity,
            String kafkaStatus
    ) {
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
