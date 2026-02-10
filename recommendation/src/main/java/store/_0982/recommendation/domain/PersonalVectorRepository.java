package store._0982.recommendation.domain;

import store._0982.common.domain.vector.PersonalVector;

import java.util.Optional;
import java.util.UUID;

public interface PersonalVectorRepository {
    Optional<PersonalVector> findById(UUID memberId);
}
