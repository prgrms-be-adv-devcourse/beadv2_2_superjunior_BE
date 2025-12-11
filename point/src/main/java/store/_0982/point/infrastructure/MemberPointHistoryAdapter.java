package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.MemberPointHistory;
import store._0982.point.domain.MemberPointHistoryRepository;

@Repository
@RequiredArgsConstructor
public class MemberPointHistoryAdapter implements MemberPointHistoryRepository {
    private final MemberPointHistoryJpaRepository historyJpaRepository;

    @Override
    public void save(MemberPointHistory memberPointHistory) {
        historyJpaRepository.save(memberPointHistory);
    }
}
