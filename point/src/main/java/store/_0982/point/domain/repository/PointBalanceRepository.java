package store._0982.point.domain.repository;

import store._0982.point.domain.entity.PointBalance;

import java.util.Optional;
import java.util.UUID;

public interface PointBalanceRepository {
    Optional<PointBalance> findById(UUID memberId);

    PointBalance save(PointBalance afterPayment);
}
