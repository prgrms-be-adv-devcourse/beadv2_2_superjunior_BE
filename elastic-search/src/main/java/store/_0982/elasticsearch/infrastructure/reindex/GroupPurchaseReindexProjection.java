package store._0982.elasticsearch.infrastructure.reindex;

import java.time.Instant;
import java.util.UUID;

public interface GroupPurchaseReindexProjection {
    UUID getGroupPurchaseId();
    String getTitle();
    String getDescription();
    String getStatus();
    Instant getStartDate();
    Instant getEndDate();
    int getMinQuantity();
    int getMaxQuantity();
    long getDiscountedPrice();
    int getCurrentQuantity();
    Instant getCreatedAt();
    Instant getUpdatedAt();
    UUID getProductId();
    String getCategory();
    Long getPrice();
    String getOriginalUrl();
    UUID getSellerId();
}
