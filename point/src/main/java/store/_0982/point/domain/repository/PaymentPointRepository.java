package store._0982.point.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.point.domain.entity.PaymentPoint;

import java.util.Optional;
import java.util.UUID;

public interface PaymentPointRepository {
    Page<PaymentPoint> findAllByMemberId(UUID memberId, Pageable pageable);

    Optional<PaymentPoint> findByOrderId(UUID orderId);

    PaymentPoint save(PaymentPoint paymentPoint);

    Optional<PaymentPoint> findById(UUID id);
}
