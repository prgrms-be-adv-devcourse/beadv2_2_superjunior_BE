package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.point.common.WebhookEvents;
import store._0982.point.domain.entity.WebhookLog;
import store._0982.point.exception.NegligibleWebhookErrorType;
import store._0982.point.exception.NegligibleWebhookException;
import store._0982.point.presentation.dto.TossWebhookRequest;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TossWebhookFacade {

    private final TossWebhookService tossWebhookService;
    private final WebhookLogService webhookLogService;

    @Transactional
    public void handleTossWebhook(int retryCount, String webhookId, OffsetDateTime transmissionTime,
                                  TossWebhookRequest request) throws JsonProcessingException {
        String eventType = request.eventType();
        if (!eventType.equals(WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED)) {
            throw new NegligibleWebhookException(NegligibleWebhookErrorType.INVALID_EVENT_TYPE);
        }

        Optional<WebhookLog> optionalWebhookLog = webhookLogService
                .getUnfinishedWebhook(webhookId, transmissionTime, request, retryCount);
        if (optionalWebhookLog.isEmpty()) {
            return;    // 이미 완료된 요청에 대해서는 예외를 반환할 필요가 없어 보임
        }

        WebhookLog webhookLog = optionalWebhookLog.get();
        try {
            tossWebhookService.processWebhookPayment(request.data());
            webhookLog.markSuccess();
        } catch (Exception e) {
            webhookLog.markFailed(e.getMessage());
            throw e;
        }
    }
}
