package store._0982.point.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.point.domain.PaymentPointFailure;
import store._0982.point.point.domain.PaymentPointFailureRepository;

@RequiredArgsConstructor
@Repository
public class PaymentPointFailureRepositoryAdapter implements PaymentPointFailureRepository {
    private final PaymentPointFailureJpaRepository repository;

    @Override
    public PaymentPointFailure save(PaymentPointFailure failure) {
        return repository.save(failure);
    }
}
