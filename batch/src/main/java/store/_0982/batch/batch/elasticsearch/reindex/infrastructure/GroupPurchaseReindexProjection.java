package store._0982.batch.batch.elasticsearch.reindex.infrastructure;

import java.time.Instant;
import java.util.UUID;

public interface GroupPurchaseReindexProjection {
    UUID getGroupPurchaseId();

    String getTitle();

    String getDescription();

    String getStatus();

    Instant getEndDate();

    long getDiscountedPrice();

    Integer getCurrentQuantity();

    Instant getUpdatedAt();

    String getCategory();

    Long getPrice();

    UUID getSellerId();
}
