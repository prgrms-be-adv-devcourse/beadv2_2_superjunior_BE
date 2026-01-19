package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.pg.PgFailCommand;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentFailure;
import store._0982.point.domain.repository.PgPaymentFailureRepository;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PgFailService {

    private final PgPaymentRepository pgPaymentRepository;
    private final PgPaymentFailureRepository pgPaymentFailureRepository;

    // TODO: 클라이언트로부터 받은 실패 데이터를 신뢰할 것인가?
    @ServiceLog
    @RetryableTransactional
    public void handlePaymentFailure(PgFailCommand command, UUID memberId) {
        PgPayment pgPayment = findFailablePayment(command.paymentKey(), memberId);
        pgPayment.markFailed();
        PgPaymentFailure pgPaymentFailure = PgPaymentFailure.pgError(pgPayment, command);
        pgPaymentFailureRepository.save(pgPaymentFailure);
    }

    private PgPayment findFailablePayment(String paymentKey, UUID memberId) {
        PgPayment pgPayment = pgPaymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        pgPayment.validateFailable(memberId);
        return pgPayment;
    }
}
