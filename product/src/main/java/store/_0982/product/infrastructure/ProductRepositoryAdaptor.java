package store._0982.product.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.product.domain.Product;
import store._0982.product.domain.ProductRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdaptor implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public void delete(Product product) {
        productJpaRepository.delete(product);
    }

}
