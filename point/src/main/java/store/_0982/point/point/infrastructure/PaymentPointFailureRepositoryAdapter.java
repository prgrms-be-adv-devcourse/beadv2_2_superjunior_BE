package store._0982.point.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.point.domain.PaymentPoint;
import store._0982.point.point.domain.PaymentPointFailure;
import store._0982.point.point.domain.PaymentPointFailureRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PaymentPointFailureRepositoryAdapter implements PaymentPointFailureRepository {
    private final PaymentPointFailureJpaRepository repository;

    @Override
    public PaymentPointFailure save(PaymentPointFailure failure) {
        return repository.save(failure);
    }

    @Override
    public Optional<PaymentPointFailure> findByPaymentPoint(PaymentPoint paymentPoint) {
        return repository.findByPaymentPoint(paymentPoint);
    }
}
