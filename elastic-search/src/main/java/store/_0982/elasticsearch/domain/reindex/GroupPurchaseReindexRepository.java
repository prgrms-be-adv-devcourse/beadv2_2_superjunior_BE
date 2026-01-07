package store._0982.elasticsearch.domain.reindex;

import store._0982.elasticsearch.domain.reindex.GroupPurchaseReindexRow;

import java.time.OffsetDateTime;
import java.util.List;

public interface GroupPurchaseReindexRepository {
    long countSource();

    List<GroupPurchaseReindexRow> fetchAllRows(int limit, long offset);

    List<GroupPurchaseReindexRow> fetchIncrementalRows(OffsetDateTime since, int limit, long offset);
}
