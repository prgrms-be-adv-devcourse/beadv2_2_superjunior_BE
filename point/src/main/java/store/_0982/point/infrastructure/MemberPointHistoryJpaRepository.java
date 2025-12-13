package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.constant.MemberPointHistoryStatus;

import java.util.UUID;

interface MemberPointHistoryJpaRepository extends JpaRepository<MemberPointHistory, UUID> {
    boolean existsByIdempotencyKeyAndStatus(UUID idempotencyKey, MemberPointHistoryStatus status);
}
