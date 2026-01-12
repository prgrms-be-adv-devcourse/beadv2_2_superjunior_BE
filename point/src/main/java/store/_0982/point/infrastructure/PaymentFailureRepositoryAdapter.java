package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.PaymentFailure;
import store._0982.point.domain.repository.PaymentFailureRepository;

@RequiredArgsConstructor
@Repository
public class PaymentFailureRepositoryAdapter implements PaymentFailureRepository {
    private final PaymentFailureJpaRepository repository;

    @Override
    public PaymentFailure save(PaymentFailure failure) {
        return repository.save(failure);
    }

}
