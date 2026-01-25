package store._0982.elasticsearch.infrastructure.search;

import java.time.Instant;
import java.util.UUID;

public interface GroupPurchaseSimilaritySearchProjection {
    UUID getGroupPurchaseId();
    int getMinQuantity();
    int getMaxQuantity();
    String getTitle();
    String getDescription();
    String getImageUrl();
    Long getDiscountedPrice();
    String getStatus();
    Instant getStartDate();
    Instant getEndDate();
    Instant getCreatedAt();
    Instant getUpdatedAt();
    int getCurrentQuantity();
    UUID getProductId();
    String getProductName();
    String getProductDescription();
    String getCategory();
    Long getPrice();
    String getOriginalUrl();
    UUID getSellerId();
    Object getProductVector();
}
