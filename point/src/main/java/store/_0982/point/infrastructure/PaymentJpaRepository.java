package store._0982.point.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import store._0982.point.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findAllByMemberId(UUID memberId, Pageable pageable);

    Optional<Payment> findByPgOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> readByPgOrderId(UUID orderId);
}
