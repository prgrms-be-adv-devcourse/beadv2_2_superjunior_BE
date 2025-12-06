package store._0982.point.point.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PaymentPointRepository {
    void save(PaymentPoint paymentPoint);

    Page<PaymentPoint> findAllByMemberId(UUID memberId, Pageable pageable);

    Optional<PaymentPoint> findByOrderId(UUID orderId);
}
