package store._0982.point.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.entity.PgPayment;

import java.util.Optional;
import java.util.UUID;

public interface PgPaymentRepository {
    Page<PgPayment> findAllByMemberId(UUID memberId, Pageable pageable);

    Optional<PgPayment> findByOrderId(UUID orderId);

    Optional<PgPayment> findById(UUID id);

    PgPayment save(PgPayment pgPayment);

    PgPayment saveAndFlush(PgPayment pgPayment);

    boolean existsByOrderIdAndStatus(UUID orderId, PgPaymentStatus status);
}
