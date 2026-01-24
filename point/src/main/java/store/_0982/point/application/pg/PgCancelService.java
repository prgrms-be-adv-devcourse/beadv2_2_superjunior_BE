package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentCancel;
import store._0982.point.domain.event.PaymentCanceledTxEvent;
import store._0982.point.domain.repository.PgPaymentCancelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PgCancelService {

    private final PgQueryService pgQueryService;
    private final PgPaymentCancelRepository pgPaymentCancelRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @RetryableTransactional
    public void markRefundedPayment(TossPaymentInfo tossPaymentInfo, UUID orderId, UUID memberId) {
        PgPayment pgPayment = pgQueryService.findRefundablePayment(orderId, memberId);

        List<String> incomingKeys = tossPaymentInfo.cancels().stream()
                .map(TossPaymentInfo.CancelInfo::transactionKey)
                .toList();
        Set<String> existingKeys = pgPaymentCancelRepository.findExistingTransactionKeys(incomingKeys);

        List<PgPaymentCancel> newCancels = new ArrayList<>();
        for (TossPaymentInfo.CancelInfo cancelInfo : tossPaymentInfo.cancels()) {
            if (!existingKeys.contains(cancelInfo.transactionKey())) {
                PgPaymentCancel pgPaymentCancel = PgPaymentCancel.from(
                        pgPayment,
                        cancelInfo.cancelReason(),
                        cancelInfo.cancelAmount(),
                        cancelInfo.canceledAt(),
                        cancelInfo.transactionKey()
                );
                newCancels.add(pgPaymentCancel);
                pgPayment.applyRefund(cancelInfo.cancelAmount(), cancelInfo.canceledAt());
            }
        }

        if (!newCancels.isEmpty()) {
            pgPaymentCancelRepository.saveAllAndFlush(newCancels);
        }
        applicationEventPublisher.publishEvent(PaymentCanceledTxEvent.from(pgPayment));
    }
}
