package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.MemberPointHistory;

import java.util.UUID;

interface MemberPointHistoryJpaRepository extends JpaRepository<MemberPointHistory, UUID> {
    boolean existsByIdempotencyKey(UUID idempotencyKey);
}
