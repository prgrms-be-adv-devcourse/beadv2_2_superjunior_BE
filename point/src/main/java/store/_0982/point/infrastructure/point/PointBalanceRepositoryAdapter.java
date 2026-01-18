package store._0982.point.infrastructure.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.repository.PointBalanceRepository;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class PointBalanceRepositoryAdapter implements PointBalanceRepository {

    private final PointBalanceJpaRepository pointBalanceJpaRepository;

    @Override
    public Optional<PointBalance> findByMemberId(UUID memberId) {
        return pointBalanceJpaRepository.findByMemberId(memberId);
    }

    @Override
    public PointBalance save(PointBalance afterPayment) {
        return pointBalanceJpaRepository.save(afterPayment);
    }
}
