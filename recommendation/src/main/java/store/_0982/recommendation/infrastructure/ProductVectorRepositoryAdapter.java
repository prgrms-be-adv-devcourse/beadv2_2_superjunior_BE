package store._0982.recommendation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.common.domain.vector.ProductVector;
import store._0982.recommendation.domain.ProductVectorRepository;

import java.util.List;
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
    public void saveAll(List<ProductVector> productVectors) {
        vectorRepository.saveAll(productVectors);
    }

    @Override
    public Optional<ProductVector> findById(UUID productId) {
        return vectorRepository.findById(productId);
    }
}
