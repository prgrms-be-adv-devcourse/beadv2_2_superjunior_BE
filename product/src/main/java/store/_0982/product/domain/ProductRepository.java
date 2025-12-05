package store._0982.product.domain;

import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Page<Product> findAll(Pageable pageable);
    Optional<Product> findById(UUID productId);
}
