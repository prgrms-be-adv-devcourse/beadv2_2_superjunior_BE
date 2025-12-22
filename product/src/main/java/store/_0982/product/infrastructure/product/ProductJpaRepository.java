package store._0982.product.infrastructure.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.product.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID>{
    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);
    List<Product> findAllByProductIdIn(List<UUID> productIds);
}
