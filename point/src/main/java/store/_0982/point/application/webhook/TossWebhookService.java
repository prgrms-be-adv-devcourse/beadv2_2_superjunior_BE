package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@RequiredArgsConstructor
public class TossWebhookService {

    private final ObjectMapper objectMapper;
    private final PgConfirmService pgConfirmService;
    private final PgCancelService pgCancelService;
    private final PgFailService pgFailService;
    private final PgPaymentRepository pgPaymentRepository;

    @Transactional
    public void processWebhookPayment(TossPaymentInfo tossPaymentInfo) throws JsonProcessingException {
        PgPayment pgPayment = pgPaymentRepository.findByOrderId(tossPaymentInfo.orderId())
                .orElseThrow(() -> new NegligibleWebhookException(NegligibleWebhookErrorType.PAYMENT_NOT_FOUND));

        switch (tossPaymentInfo.status()) {
            case ABORTED, EXPIRED -> handleFailed(pgPayment, tossPaymentInfo);
            case CANCELED -> handleCanceled(pgPayment, tossPaymentInfo);
            case PARTIAL_CANCELED -> handlePartiallyCanceled(pgPayment, tossPaymentInfo);
            case DONE -> handleCompleted(pgPayment, tossPaymentInfo);
            case IN_PROGRESS -> handleInProgress(pgPayment, tossPaymentInfo);
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
            // TODO: 이런 케이스가 존재할까? 그런데 중요한 사항이니 고려해보긴 해야 할 것 같다
            case COMPLETED -> throw new NegligibleWebhookException(NegligibleWebhookErrorType.PAYMENT_STATUS_MISMATCH);
        }
    }

    private void handleCanceled(PgPayment pgPayment, TossPaymentInfo tossPaymentInfo) {
        // TODO: 환불이 여러 번 일어났을 때 (cancels의 요소가 여러 개)를 생각하면 수정이 필요하다
        switch (pgPayment.getStatus()) {
            case FAILED, REFUNDED -> {
                // 무시
            }
            // 토스에서 전액 취소로 정보가 왔음 -> 따로 환불 로직은 필요 없음
            case PENDING -> pgPayment.markRefunded(tossPaymentInfo.cancels().get(0).canceledAt());

            case COMPLETED, PARTIALLY_REFUNDED -> pgCancelService
                    .markRefundedPayment(pgPayment.getMemberId(), pgPayment.getOrderId(), tossPaymentInfo);
        }
    }

    private void handlePartiallyCanceled(PgPayment pgPayment, TossPaymentInfo tossPaymentInfo) {
        switch (pgPayment.getStatus()) {
            // 전체 환불도 아니고 부분 환불? -> 생각보다 상태 충돌로 간주될 부분이 많아 보인다
            case PENDING, FAILED, REFUNDED ->
                    throw new NegligibleWebhookException(NegligibleWebhookErrorType.PAYMENT_STATUS_MISMATCH);

            case COMPLETED, PARTIALLY_REFUNDED -> pgCancelService
                    .markRefundedPayment(pgPayment.getMemberId(), pgPayment.getOrderId(), tossPaymentInfo);
        }
    }

    private void handleCompleted(PgPayment pgPayment, TossPaymentInfo tossPaymentInfo) {
        switch (pgPayment.getStatus()) {
            case COMPLETED, REFUNDED, PARTIALLY_REFUNDED, FAILED -> {
                // 무시
            }
            case PENDING -> pgConfirmService
                    .markConfirmedPayment(pgPayment.getMemberId(), pgPayment.getOrderId(), tossPaymentInfo);
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
