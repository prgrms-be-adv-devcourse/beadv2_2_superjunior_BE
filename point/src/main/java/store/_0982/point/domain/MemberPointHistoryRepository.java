package store._0982.point.domain;

import store._0982.point.domain.constant.MemberPointHistoryStatus;

import java.util.UUID;

public interface MemberPointHistoryRepository {
    void save(MemberPointHistory memberPointHistory);

    boolean existsByIdempotencyKeyAndStatus(UUID idempotencyKey, MemberPointHistoryStatus status);
}
