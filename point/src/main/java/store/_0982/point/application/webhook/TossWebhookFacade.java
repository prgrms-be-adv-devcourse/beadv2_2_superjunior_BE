package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.point.common.WebhookEvents;
import store._0982.point.domain.entity.WebhookLog;
import store._0982.point.exception.CustomErrorCode;
import store._0982.point.presentation.dto.TossWebhookRequest;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TossWebhookFacade {

    private final TossWebhookService tossWebhookService;
    private final WebhookLogService webhookLogService;

    public void handleTossWebhook(int retryCount, String webhookId, OffsetDateTime transmissionTime,
                                  TossWebhookRequest request) throws JsonProcessingException {
        String eventType = request.eventType();
        if (!eventType.equals(WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED)) {
            throw new CustomException(CustomErrorCode.INVALID_WEBHOOK);
        }

        WebhookLog webhookLog = webhookLogService.findByWebhookId(webhookId)
                .orElseGet(() -> {
                    try {
                        return webhookLogService.createWebhookLog(webhookId, transmissionTime, request, retryCount);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("웹훅 로그 생성 실패", e);
                    }
                });

        if (webhookLog.isAlreadyProcessed()) {
            return;
        }

        webhookLogService.updateRetryCount(webhookId, retryCount);

        try {
            webhookLogService.markProcessing(webhookId);
            tossWebhookService.processWebhookPayment(request.data());
            webhookLogService.markSuccess(webhookId);
        } catch (Exception e) {
            webhookLogService.markFailed(webhookId, e.getMessage());
            throw e;
        }
    }
}
