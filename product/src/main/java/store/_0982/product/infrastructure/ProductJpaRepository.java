package store._0982.product.infrastructure;

import store._0982.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID>{
    List<Product> findAllByProductIdIn(List<UUID> productIds);
}
