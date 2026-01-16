package store._0982.point.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointTransaction;

import java.util.Optional;
import java.util.UUID;

public interface PointTransactionRepository {

    PointTransaction save(PointTransaction pointTransaction);

    boolean existsByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOrderIdAndStatus(UUID orderId, PointTransactionStatus status);

    PointTransaction saveAndFlush(PointTransaction pointTransaction);

    Optional<PointTransaction> findByOrderIdAndStatus(UUID orderId, PointTransactionStatus status);

    Page<PointTransaction> findByMemberId(UUID memberId, Pageable pageable);
}
