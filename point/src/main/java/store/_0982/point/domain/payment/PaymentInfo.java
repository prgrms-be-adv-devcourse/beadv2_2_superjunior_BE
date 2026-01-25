package store._0982.point.domain.payment;

import store._0982.point.domain.constant.PaymentMethod;

public interface PaymentInfo {

    PaymentMethod paymentMethod();

    long amount();
}
