package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.point.application.dto.pg.PgCancelCommand;
import store._0982.point.application.dto.pg.PgConfirmCommand;
import store._0982.point.application.dto.pg.PgFailCommand;
import store._0982.point.application.pg.PgCancelService;
import store._0982.point.application.pg.PgConfirmService;
import store._0982.point.application.pg.PgFailService;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.NegligibleWebhookErrorType;
import store._0982.point.exception.NegligibleWebhookException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TossWebhookService {

    private final ObjectMapper objectMapper;
    private final PgConfirmService pgConfirmService;
    private final PgCancelService pgCancelService;
    private final PgFailService pgFailService;
    private final PgPaymentRepository pgPaymentRepository;

    @Transactional
    public void processWebhookPayment(TossPaymentInfo paymentData) throws JsonProcessingException {
        PgPayment pgPayment = pgPaymentRepository.findByOrderId(paymentData.orderId())
                .orElseThrow(() -> new NegligibleWebhookException(NegligibleWebhookErrorType.PAYMENT_NOT_FOUND));

        switch (paymentData.status()) {
            case ABORTED, EXPIRED -> handleFailed(pgPayment, paymentData);
            case CANCELED -> handleCanceled(pgPayment, paymentData);
            case PARTIAL_CANCELED -> handlePartiallyCanceled(pgPayment, paymentData);
            case DONE -> handleCompleted(pgPayment, paymentData);
            case IN_PROGRESS -> handleInProgress(pgPayment, paymentData);
            case READY, WAITING_FOR_DEPOSIT -> {
                // 무시
            }
        }
    }

    private void handleFailed(PgPayment pgPayment, TossPaymentInfo tossPaymentInfo) throws JsonProcessingException {
        switch (pgPayment.getStatus()) {
            case FAILED, PARTIALLY_REFUNDED, REFUNDED -> {
                // 무시
            }
            case PENDING -> {
                PgFailCommand command = new PgFailCommand(
                        pgPayment.getOrderId(),
                        pgPayment.getPaymentKey(),
                        tossPaymentInfo.failure().code(),
                        tossPaymentInfo.failure().message(),
                        pgPayment.getAmount(),
                        objectMapper.writeValueAsString(tossPaymentInfo)
                );
                pgFailService.handlePaymentFailure(command, pgPayment.getMemberId());
            }
            case COMPLETED -> {
                // TODO: 이런 케이스가 존재할까? 그런데 중요한 사항이니 고려해보긴 해야 할 것 같다
            }
        }
    }

    private void handleCanceled(PgPayment pgPayment, TossPaymentInfo tossPaymentInfo) {
        switch (pgPayment.getStatus()) {
            case FAILED, REFUNDED -> {
                // 무시
            }
            case PENDING, PARTIALLY_REFUNDED -> {
                // 토스에서 전액 취소로 정보가 왔음 -> 따로 환불 로직은 필요 없음
                List<TossPaymentInfo.CancelInfo> cancels = tossPaymentInfo.cancels();
                pgPayment.markRefunded(cancels.get(cancels.size() - 1).canceledAt());
            }
            case COMPLETED -> {
                List<TossPaymentInfo.CancelInfo> cancels = tossPaymentInfo.cancels();
                TossPaymentInfo.CancelInfo cancelInfo = cancels.get(cancels.size() - 1);
                PgCancelCommand command = new PgCancelCommand(
                        pgPayment.getOrderId(),
                        cancelInfo.cancelReason(),
                        cancelInfo.cancelAmount()
                );
                pgCancelService.refundPaymentPoint(pgPayment.getMemberId(), command);
            }
        }
    }

    private void handlePartiallyCanceled(PgPayment pgPayment, TossPaymentInfo tossPaymentInfo) {

    }

    private void handleCompleted(PgPayment pgPayment, TossPaymentInfo tossPaymentInfo) {
        switch (pgPayment.getStatus()) {
            case COMPLETED, REFUNDED, PARTIALLY_REFUNDED, FAILED -> {
                // 무시
            }
            case PENDING -> pgPayment.markConfirmed(
                    tossPaymentInfo.paymentMethod(),
                    tossPaymentInfo.approvedAt(),
                    tossPaymentInfo.paymentKey()
            );
        }
    }

    private void handleInProgress(PgPayment pgPayment, TossPaymentInfo tossPaymentInfo) {
        switch (pgPayment.getStatus()) {
            case COMPLETED, FAILED, REFUNDED, PARTIALLY_REFUNDED -> {
                // 무시
            }
            case PENDING -> {
                PgConfirmCommand command = new PgConfirmCommand(
                        pgPayment.getOrderId(),
                        pgPayment.getAmount(),
                        tossPaymentInfo.paymentKey()
                );
                pgConfirmService.confirmPayment(command, pgPayment.getMemberId());
            }
        }
    }

}
