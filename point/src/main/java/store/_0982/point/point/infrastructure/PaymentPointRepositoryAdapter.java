package store._0982.point.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.point.domain.PaymentPoint;
import store._0982.point.point.domain.PaymentPointRepository;

@Repository
@RequiredArgsConstructor
public class PaymentPointRepositoryAdapter implements PaymentPointRepository {

    private final PaymentPointJpaRepository paymentPointJpaRepository;

    @Override
    public PaymentPoint save(PaymentPoint paymentPoint) {
        return paymentPointJpaRepository.save(paymentPoint);
    }
}
