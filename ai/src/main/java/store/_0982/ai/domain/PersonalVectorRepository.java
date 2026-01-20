package store._0982.ai.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonalVectorRepository {
    Optional<PersonalVector> findById(UUID memberId);
}
