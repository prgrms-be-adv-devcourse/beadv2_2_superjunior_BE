package store._0982.recommendation.application;

import java.util.List;
import java.util.UUID;

interface CacheService {

    List<UUID> refreshProductListCache(UUID memberId);

    List<UUID> getProductListCache(UUID memberId);
}
