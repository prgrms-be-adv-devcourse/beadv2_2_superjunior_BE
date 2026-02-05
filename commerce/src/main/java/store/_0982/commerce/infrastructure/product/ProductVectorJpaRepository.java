package store._0982.commerce.infrastructure.product;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.commerce.domain.product.ProductVector;

import java.util.List;
import java.util.UUID;

public interface ProductVectorJpaRepository extends JpaRepository<ProductVector, UUID> {
    List<ProductVector> findByProductIdIn(List<UUID> productIds);
}
