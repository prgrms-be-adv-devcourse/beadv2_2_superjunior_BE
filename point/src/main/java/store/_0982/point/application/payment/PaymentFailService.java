package store._0982.point.application.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PaymentFailCommand;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentFailService {

    private final PaymentTransactionManager paymentTransactionManager;

    // TODO: 클라이언트로부터 받은 실패 데이터를 신뢰할 것인가?
    @ServiceLog
    @Transactional
    public void handlePaymentFailure(PaymentFailCommand command, UUID memberId) {
        paymentTransactionManager.markFailedPaymentByPg(command, memberId);
    }
}
