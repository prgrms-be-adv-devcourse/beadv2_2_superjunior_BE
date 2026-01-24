package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.pg.PgFailCommand;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentFailure;
import store._0982.point.domain.event.PaymentFailedTxEvent;
import store._0982.point.domain.repository.PgPaymentFailureRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PgFailService {

    private final PgPaymentFailureRepository pgPaymentFailureRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PgReadManager pgReadManager;

    // TODO: 클라이언트로부터 받은 실패 데이터를 신뢰할 것인가?
    @ServiceLog
    @RetryableTransactional
    public void handlePaymentFailure(PgFailCommand command, UUID memberId) {
        PgPayment pgPayment = pgReadManager.findFailablePayment(command.orderId(), memberId);
        pgPayment.markFailed(command.paymentKey());
        PgPaymentFailure pgPaymentFailure = PgPaymentFailure.pgError(pgPayment, command);
        pgPaymentFailureRepository.save(pgPaymentFailure);
        applicationEventPublisher.publishEvent(PaymentFailedTxEvent.from(pgPayment));
    }
}
