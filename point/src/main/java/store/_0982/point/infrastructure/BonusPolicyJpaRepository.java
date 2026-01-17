package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.BonusPolicy;

import java.util.UUID;

public interface BonusPolicyJpaRepository extends JpaRepository<BonusPolicy, UUID>, BonusPolicyRepositoryCustom {
}
