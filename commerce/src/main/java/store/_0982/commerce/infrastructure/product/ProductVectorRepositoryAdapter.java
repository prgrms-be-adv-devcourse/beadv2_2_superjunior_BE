package store._0982.commerce.infrastructure.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.commerce.domain.product.ProductVectorRepository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductVectorRepositoryAdapter implements ProductVectorRepository {

    private final ProductVectorJpaRepository vectorRepository;

    @Override
    public void deleteById(UUID productId) {
        vectorRepository.deleteById(productId);
    }
}
