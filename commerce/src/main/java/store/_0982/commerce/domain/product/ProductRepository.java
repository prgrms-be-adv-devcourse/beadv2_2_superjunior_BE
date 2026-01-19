package store._0982.commerce.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID productId);

    void delete(Product product);

    Product saveAndFlush(Product product);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    Optional<Product> findByIdempotencyKey(String idempotencyKey);
}
