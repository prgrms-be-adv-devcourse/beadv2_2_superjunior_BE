package store._0982.point.point.domain;

import java.util.Optional;

public interface PaymentPointFailureRepository {
    PaymentPointFailure save(PaymentPointFailure failure);

    Optional<PaymentPointFailure> findByPaymentPoint(PaymentPoint paymentPoint);
}
