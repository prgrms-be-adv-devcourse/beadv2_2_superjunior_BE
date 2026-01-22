package store._0982.batch.domain.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID productId);
    List<Product> findAllByIdIn(List<UUID> ids);
}
