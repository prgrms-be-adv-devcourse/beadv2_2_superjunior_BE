package store._0982.common.kafka.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.UUID;

@Getter
public class GroupPurchaseEvent extends BaseEvent {
    private final UUID id;
    private final int minQuantity;
    private final Integer maxQuantity;
    private final String title;
    private final String description;
    private final long discountedPrice;
    private final String status;
    private final String sellerName;
    private final String productName;
    private final String startDate;
    private final String endDate;
    private final String createdAt;
    private final String updatedAt;
    private final Integer currentQuantity;

    @JsonCreator
    public GroupPurchaseEvent(
            @JsonProperty("id") UUID id,
            @JsonProperty("minQuantity") int minQuantity,
            @JsonProperty("maxQuantity") Integer maxQuantity,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("discountedPrice") long discountedPrice,
            @JsonProperty("status") String status,
            @JsonProperty("sellerName") String sellerName,
            @JsonProperty("productName") String productName,
            @JsonProperty("startDate") String startDate,
            @JsonProperty("endDate") String endDate,
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("updatedAt") String updatedAt,
            @JsonProperty("currentQuantity") int currentQuantity
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
    }
}
