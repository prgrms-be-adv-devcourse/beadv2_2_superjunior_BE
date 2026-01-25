package store._0982.point.infrastructure.point;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, UUID>, PointTransactionRepositoryCustom {
    boolean existsByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOrderIdAndStatus(UUID orderId, PointTransactionStatus status);

    Optional<PointTransaction> findByOrderIdAndStatus(UUID orderId, PointTransactionStatus status);

    Page<PointTransaction> findByMemberId(UUID memberId, Pageable pageable);

    List<PointTransaction> findAllByOrderId(UUID orderId);
}
