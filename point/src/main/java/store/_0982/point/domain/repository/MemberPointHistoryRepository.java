package store._0982.point.domain.repository;

import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.constant.MemberPointHistoryStatus;

import java.util.UUID;

public interface MemberPointHistoryRepository {
    MemberPointHistory save(MemberPointHistory memberPointHistory);

    boolean existsByIdempotencyKeyAndStatus(UUID idempotencyKey, MemberPointHistoryStatus status);
}
