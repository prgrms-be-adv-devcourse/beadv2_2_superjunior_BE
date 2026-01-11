package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PaymentFailCommand;
import store._0982.point.application.dto.PaymentInfo;
import store._0982.point.domain.entity.Payment;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentFailService {

    private final PaymentTransactionManager paymentTransactionManager;

    // TODO: 클라이언트로부터 받은 실패 데이터를 신뢰할 것인가?
    @ServiceLog
    @Transactional
    public PaymentInfo handlePaymentFailure(PaymentFailCommand command, UUID memberId) {
        Payment payment = paymentTransactionManager.markFailedPaymentByPg(command, memberId);
        return PaymentInfo.from(payment);
    }
}
