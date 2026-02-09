package store._0982.batch.infrastructure.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.recommendation.PersonalVectorRepository;
import store._0982.common.domain.vector.PersonalVector;

@Repository
@RequiredArgsConstructor
public class PersonalVectorRepositoryAdapter implements PersonalVectorRepository {
    private final PersonalVectorJpaRepository personalVectorJpaRepository;

    @Override
    public void saveAll(Iterable<? extends PersonalVector> vectors) {
        personalVectorJpaRepository.saveAll(vectors);
    }
}
