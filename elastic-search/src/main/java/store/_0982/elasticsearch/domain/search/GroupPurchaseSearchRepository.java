package store._0982.elasticsearch.domain.search;

import java.util.List;
import java.util.UUID;

public interface GroupPurchaseSearchRepository {
    List<GroupPurchaseSearchRow> findAllByIds(List<UUID> groupPurchaseIds);

    List<GroupPurchaseSimilaritySearchRow> findAllSimilarityByIds(List<UUID> groupPurchaseIds);
}
