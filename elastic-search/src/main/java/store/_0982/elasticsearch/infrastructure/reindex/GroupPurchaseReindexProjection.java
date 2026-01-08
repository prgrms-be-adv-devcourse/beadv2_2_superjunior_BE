package store._0982.elasticsearch.infrastructure.reindex;

import java.time.Instant;
import java.util.UUID;

public interface GroupPurchaseReindexProjection {
    UUID getGroupPurchaseId();
    String getTitle();
    String getDescription();
    String getStatus();
    Instant getUpdatedAt();
    String getCategory();
    UUID getSellerId();
}
