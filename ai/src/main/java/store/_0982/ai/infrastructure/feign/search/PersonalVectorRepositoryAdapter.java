package store._0982.ai.infrastructure.feign.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.ai.domain.PersonalVector;
import store._0982.ai.domain.PersonalVectorRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PersonalVectorRepositoryAdapter implements PersonalVectorRepository {

    private final PersonalVectorJpaRepository personalVectorJpaRepository;

    @Override
    public Optional<PersonalVector> findById(UUID memberId) {
        return personalVectorJpaRepository.findById(memberId);
    }
}
