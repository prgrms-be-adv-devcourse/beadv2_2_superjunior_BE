package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.repository.MemberPointHistoryRepository;
import store._0982.point.domain.constant.MemberPointHistoryStatus;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MemberPointHistoryAdapter implements MemberPointHistoryRepository {
    private final MemberPointHistoryJpaRepository historyJpaRepository;

    @Override
    public MemberPointHistory save(MemberPointHistory memberPointHistory) {
        return historyJpaRepository.save(memberPointHistory);
    }

    @Override
    public boolean existsByIdempotencyKeyAndStatus(UUID idempotencyKey, MemberPointHistoryStatus status) {
        return historyJpaRepository.existsByIdempotencyKeyAndStatus(idempotencyKey, status);
    }
}
