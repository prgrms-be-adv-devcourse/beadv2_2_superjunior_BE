package store._0982.product.domain;

import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID productId);

    void delete(Product product);

    Page<Product> findAll(Pageable pageable);

    Product saveAndFlush(Product product);
}
