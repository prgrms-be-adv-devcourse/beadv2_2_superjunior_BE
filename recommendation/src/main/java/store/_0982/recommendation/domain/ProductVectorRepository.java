package store._0982.recommendation.domain;

import store._0982.common.domain.vector.ProductVector;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVectorRepository {

    void save(ProductVector productVector);

    void saveAll(List<ProductVector> productVectors);

    Optional<ProductVector> findById(UUID productId);
}
