package store._0982.elasticsearch.infrastructure.search;

import java.time.Instant;
import java.util.UUID;

public interface GroupPurchaseSearchProjection {
    UUID getGroupPurchaseId();
    int getMinQuantity();
    int getMaxQuantity();
    String getTitle();
    String getDescription();
    Long getDiscountedPrice();
    String getStatus();
    Instant getStartDate();
    Instant getEndDate();
    Instant getCreatedAt();
    Instant getUpdatedAt();
    int getCurrentQuantity();
    UUID getProductId();
    String getCategory();
    Long getPrice();
    String getOriginalUrl();
    UUID getSellerId();
}
