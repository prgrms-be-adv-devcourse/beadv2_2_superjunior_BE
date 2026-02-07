package store._0982.recommendation.domain;

import java.util.Optional;
import java.util.UUID;

public interface ProductVectorRepository {

    void save(ProductVector productVector);

    Optional<ProductVector> findById(UUID productId);
}
