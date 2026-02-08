package store._0982.batch.infrastructure.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.batch.domain.recommendation.PersonalVector;

import java.util.UUID;

public interface PersonalVectorJpaRepository extends JpaRepository<PersonalVector, UUID> {
}
