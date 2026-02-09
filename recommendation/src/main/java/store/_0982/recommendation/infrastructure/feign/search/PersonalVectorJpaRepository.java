package store._0982.recommendation.infrastructure.feign.search;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.recommendation.domain.PersonalVector;

import java.util.UUID;

public interface PersonalVectorJpaRepository extends JpaRepository<PersonalVector, UUID> {
}
