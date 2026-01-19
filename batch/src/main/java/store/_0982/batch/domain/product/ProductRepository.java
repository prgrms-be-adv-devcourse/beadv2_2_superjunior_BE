package store._0982.batch.domain.product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID productId);
}
