package store._0982.recommendation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.common.domain.vector.ProductVector;
import store._0982.recommendation.domain.ProductVectorRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductVectorRepositoryAdapter implements ProductVectorRepository {

    private final ProductVectorJpaRepository vectorRepository;

    @Override
    public void save(ProductVector productVector) {
        vectorRepository.save(productVector);
    }

    @Override
    public Optional<ProductVector> findById(UUID productId) {
        return vectorRepository.findById(productId);
    }
}
