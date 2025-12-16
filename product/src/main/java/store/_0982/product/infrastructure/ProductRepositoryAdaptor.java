package store._0982.product.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store._0982.product.domain.Product;
import store._0982.product.domain.ProductRepository;

import java.util.List;
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
    public Page<Product> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public Product saveAndFlush(Product product) {
        return productJpaRepository.saveAndFlush(product);
    }

    @Override
    public Page<Product> findBySellerId(UUID sellerId, Pageable pageable) {
        return productJpaRepository.findBySellerId(sellerId,pageable);
    }
    public Optional<Product> findById(UUID productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public void delete(Product product) {
        productJpaRepository.delete(product);
    }

    @Override
    public List<Product> findAllByProductIdIn(List<UUID> productIds) {
        return productJpaRepository.findAllByProductIdIn(productIds);
    }

}
