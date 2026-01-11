package store._0982.elasticsearch.domain.reindex;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface GroupPurchaseReindexRepository {
    long countSource();

    List<GroupPurchaseReindexRow> fetchAllRows(int limit, long offset);

    List<GroupPurchaseReindexRow> fetchIncrementalRows(OffsetDateTime since, int limit, long offset);

    List<GroupPurchaseReindexRow> fetchByIds(List<UUID> ids);
}
