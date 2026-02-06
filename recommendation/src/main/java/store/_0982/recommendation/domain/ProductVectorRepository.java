package store._0982.recommendation.domain;

import java.util.UUID;

public interface ProductVectorRepository {
    void deleteById(UUID productId);

    void save(ProductVector productVector);
}
