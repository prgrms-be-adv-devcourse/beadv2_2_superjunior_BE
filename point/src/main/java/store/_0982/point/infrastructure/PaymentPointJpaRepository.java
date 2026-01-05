package store._0982.point.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import store._0982.point.domain.entity.PaymentPoint;

import java.util.Optional;
import java.util.UUID;

public interface PaymentPointJpaRepository extends JpaRepository<PaymentPoint, UUID> {
    Page<PaymentPoint> findAllByMemberId(UUID memberId, Pageable pageable);

    Optional<PaymentPoint> findByPgOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PaymentPoint> readByPgOrderId(UUID orderId);
}
