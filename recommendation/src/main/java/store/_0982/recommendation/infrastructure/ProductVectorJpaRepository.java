package store._0982.recommendation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.recommendation.domain.ProductVector;


import java.util.List;
import java.util.UUID;

public interface ProductVectorJpaRepository extends JpaRepository<ProductVector, UUID> {
    List<ProductVector> findByProductIdIn(List<UUID> productIds);
}
