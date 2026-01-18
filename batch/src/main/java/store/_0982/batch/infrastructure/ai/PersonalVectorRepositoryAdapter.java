package store._0982.batch.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.ai.PersonalVectorRepository;

@Repository
@RequiredArgsConstructor
public class PersonalVectorRepositoryAdapter implements PersonalVectorRepository {
    private final PersonalVectorJpaRepository personalVectorJpaRepository;

}
