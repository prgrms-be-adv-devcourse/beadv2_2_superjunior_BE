package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.event.PaymentConfirmedEvent;
import store._0982.point.domain.repository.PaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class PaymentTransactionManager {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    Payment findPayment(String paymentKey) {
        return paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
    }

    Payment findCompletablePayment(String paymentKey, UUID memberId) {
        Payment payment = findPayment(paymentKey);
        payment.validateCompletable(memberId);
        return payment;
    }

    Payment findFailablePayment(String paymentKey, UUID memberId) {
        Payment payment = findPayment(paymentKey);
        payment.validateFailable(memberId);
        return payment;
    }

    @Transactional
    void markConfirmedPayment(TossPaymentResponse tossPaymentResponse, String paymentKey, UUID memberId) {
        Payment payment = findCompletablePayment(paymentKey, memberId);
        payment.markConfirmed(tossPaymentResponse.method(), tossPaymentResponse.approvedAt(), tossPaymentResponse.paymentKey());
        applicationEventPublisher.publishEvent(PaymentConfirmedEvent.from(payment));
    }

    @Transactional
    void markFailedPayment(String errorMessage, String paymentKey) {
        Payment payment = findPayment(paymentKey);
        payment.markFailed(errorMessage);
    }
}
