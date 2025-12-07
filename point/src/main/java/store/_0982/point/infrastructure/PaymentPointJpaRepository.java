package store._0982.point.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.PaymentPoint;

import java.util.Optional;
import java.util.UUID;

interface PaymentPointJpaRepository extends JpaRepository<PaymentPoint, UUID> {
    Page<PaymentPoint> findAllByMemberId(UUID memberId, Pageable pageable);

    Optional<PaymentPoint> findByPgOrderId(UUID orderId);
}
