package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.constant.MemberPointHistoryStatus;
import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.repository.MemberPointHistoryRepository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MemberPointHistoryRepositoryAdapter implements MemberPointHistoryRepository {
    private final MemberPointHistoryJpaRepository historyJpaRepository;

    @Override
    public MemberPointHistory save(MemberPointHistory memberPointHistory) {
        return historyJpaRepository.save(memberPointHistory);
    }

    @Override
    public boolean existsByIdempotencyKey(UUID idempotencyKey) {
        return historyJpaRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public boolean existsByOrderIdAndStatus(UUID orderId, MemberPointHistoryStatus status) {
        return historyJpaRepository.existsByOrderIdAndStatus(orderId, status);
    }

    @Override
    public long count() {
        return historyJpaRepository.count();
    }

    @Override
    public void deleteAll() {
        historyJpaRepository.deleteAll();
    }

    @Override
    public MemberPointHistory saveAndFlush(MemberPointHistory memberPointHistory) {
        return historyJpaRepository.saveAndFlush(memberPointHistory);
    }
}
