package store._0982.ai.domain;

import java.util.Optional;
import java.util.UUID;

public interface PersonalVectorRepository {
    Optional<PersonalVector> findById(UUID memberId);
}
