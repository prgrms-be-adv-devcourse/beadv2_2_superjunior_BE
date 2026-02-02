package store._0982.commerce.infrastructure.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.commerce.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID>{
    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    Optional<Product> findByIdempotencyKey(String idempotencyKey);

    List<Product> findByProductIdIn(List<UUID> productIds);
}
