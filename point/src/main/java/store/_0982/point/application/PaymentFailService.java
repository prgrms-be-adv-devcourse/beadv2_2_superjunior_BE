package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PaymentInfo;
import store._0982.point.application.dto.PaymentFailCommand;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.entity.PaymentFailure;
import store._0982.point.domain.repository.PaymentFailureRepository;
import store._0982.point.domain.repository.PaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentFailService {

    private final PaymentRepository paymentRepository;
    private final PaymentFailureRepository paymentFailureRepository;

    @ServiceLog
    @Transactional
    public PaymentInfo handlePaymentFailure(PaymentFailCommand command, UUID memberId) {
        Payment payment = paymentRepository.findByPaymentKey(command.paymentKey())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        payment.validateFailable(memberId);
        payment.markFailed(command.errorMessage());

        PaymentFailure failure = PaymentFailure.from(payment, command);
        paymentFailureRepository.save(failure);
        return PaymentInfo.from(payment);
    }
}
