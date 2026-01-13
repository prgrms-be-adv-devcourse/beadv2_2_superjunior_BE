package store._0982.point.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.PgPayment;

import java.util.Optional;
import java.util.UUID;

public interface PgPaymentJpaRepository extends JpaRepository<PgPayment, UUID> {
    Page<PgPayment> findAllByMemberId(UUID memberId, Pageable pageable);

    Optional<PgPayment> findByOrderId(UUID orderId);

    Optional<PgPayment> findByPaymentKey(String paymentKey);
}
