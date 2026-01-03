package store._0982.point.domain.repository;

import store._0982.point.domain.constant.MemberPointHistoryStatus;
import store._0982.point.domain.entity.MemberPointHistory;

import java.util.UUID;

public interface MemberPointHistoryRepository {
    MemberPointHistory save(MemberPointHistory memberPointHistory);

    boolean existsByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOrderIdAndStatus(UUID orderId, MemberPointHistoryStatus status);

    MemberPointHistory saveAndFlush(MemberPointHistory memberPointHistory);
}
