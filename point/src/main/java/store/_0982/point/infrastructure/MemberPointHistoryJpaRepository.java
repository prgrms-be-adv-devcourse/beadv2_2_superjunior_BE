package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.constant.MemberPointHistoryStatus;
import store._0982.point.domain.entity.MemberPointHistory;

import java.util.UUID;

public interface MemberPointHistoryJpaRepository extends JpaRepository<MemberPointHistory, UUID> {
    boolean existsByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOrderIdAndStatus(UUID orderId, MemberPointHistoryStatus status);
}
