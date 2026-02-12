package store._0982.batch.domain.elasticsearch;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface GroupPurchaseReindexRepository {
    long countSource();

    List<GroupPurchaseReindexRow> fetchAllRows(int limit, UUID lastId);

    List<GroupPurchaseReindexRow> fetchIncrementalRows(OffsetDateTime since, int limit, UUID lastId);

    List<GroupPurchaseReindexRow> fetchByIds(List<UUID> ids);
}
