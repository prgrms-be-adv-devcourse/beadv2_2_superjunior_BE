package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.PaymentMethodDetailMapper;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.event.PaymentConfirmedTxEvent;
import store._0982.point.domain.vo.PaymentMethodDetail;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PgConfirmService {

    private final PgQueryService pgQueryService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @RetryableTransactional
    public void markConfirmedPayment(TossPaymentInfo tossPaymentInfo, UUID orderId, UUID memberId) {
        PgPayment pgPayment = pgQueryService.findCompletablePayment(orderId, memberId);
        PaymentMethodDetail paymentMethodDetail = PaymentMethodDetailMapper.from(tossPaymentInfo);
        pgPayment.markConfirmed(
                tossPaymentInfo.paymentMethod(),
                paymentMethodDetail,
                tossPaymentInfo.approvedAt(),
                tossPaymentInfo.paymentKey()
        );
        applicationEventPublisher.publishEvent(PaymentConfirmedTxEvent.from(pgPayment));
    }
}
