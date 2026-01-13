package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.PointBalance;

import java.util.UUID;

public interface PointBalanceJpaRepository extends JpaRepository<PointBalance, UUID> {
}
