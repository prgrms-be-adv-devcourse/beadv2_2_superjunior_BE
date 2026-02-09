package store._0982.batch.infrastructure.recommendation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductVectorQueryRepository {
    Map<UUID, float[]> findVectorsByProductIds(List<UUID> productIds);
}
