package store._0982.batch.infrastructure.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.batch.domain.ai.PersonalVector;

import java.util.UUID;

public interface PersonalVectorJpaRepository extends JpaRepository<PersonalVector, UUID> {
}
