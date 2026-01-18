package store._0982.point.infrastructure.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.repository.PgPaymentRepository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PgPaymentRepositoryAdapter implements PgPaymentRepository {

    private final PgPaymentJpaRepository pgPaymentJpaRepository;

    @Override
    public Page<PgPayment> findAllByMemberId(UUID memberId, Pageable pageable) {
        return pgPaymentJpaRepository.findAllByMemberId(memberId, pageable);
    }

    @Override
    public Optional<PgPayment> findByOrderId(UUID orderId) {
        return pgPaymentJpaRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<PgPayment> findByPaymentKey(String paymentKey) {
        return pgPaymentJpaRepository.findByPaymentKey(paymentKey);
    }

    @Override
    public PgPayment save(PgPayment pgPayment) {
        return pgPaymentJpaRepository.save(pgPayment);
    }

    @Override
    public PgPayment saveAndFlush(PgPayment pgPayment) {
        return pgPaymentJpaRepository.saveAndFlush(pgPayment);
    }

    @Override
    public Optional<PgPayment> findById(UUID id) {
        return pgPaymentJpaRepository.findById(id);
    }
}
