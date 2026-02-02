package store._0982.point.infrastructure.point;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.PointBalance;

import java.util.Optional;
import java.util.UUID;

public interface PointBalanceJpaRepository extends JpaRepository<PointBalance, UUID> {
    Optional<PointBalance> findByMemberId(UUID memberId);

    void deleteByMemberId(UUID memberId);
}
