package store._0982.batch.infrastructure.product;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.batch.domain.product.Product;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID>{
}
