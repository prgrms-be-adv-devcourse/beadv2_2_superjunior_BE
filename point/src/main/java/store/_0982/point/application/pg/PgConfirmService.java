package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.event.PaymentConfirmedTxEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PgConfirmService {

    private final PgQueryService pgQueryService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @RetryableTransactional
    public void markConfirmedPayment(TossPaymentInfo tossPaymentInfo, UUID orderId, UUID memberId) {
        PgPayment pgPayment = pgQueryService.findCompletablePayment(orderId, memberId);
        pgPayment.markConfirmed(tossPaymentInfo.paymentMethod(), tossPaymentInfo.approvedAt(), tossPaymentInfo.paymentKey());
        applicationEventPublisher.publishEvent(PaymentConfirmedTxEvent.from(pgPayment));
    }
}
