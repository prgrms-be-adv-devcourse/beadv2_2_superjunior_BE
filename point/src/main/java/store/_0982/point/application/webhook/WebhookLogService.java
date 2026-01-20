package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.point.domain.entity.WebhookLog;
import store._0982.point.domain.repository.WebhookLogRepository;
import store._0982.point.presentation.dto.TossWebhookRequest;

import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebhookLogService {

    private final WebhookLogRepository webhookLogRepository;
    private final ObjectMapper objectMapper;

    public Optional<WebhookLog> findByWebhookId(String webhookId) {
        return webhookLogRepository.findByWebhookId(webhookId);
    }

    @Transactional
    public WebhookLog createWebhookLog(String webhookId, OffsetDateTime transmissionTime,
                                       TossWebhookRequest request, int retryCount) throws JsonProcessingException {
        WebhookLog webhookLog = WebhookLog.create(
                webhookId,
                request.eventType(),
                objectMapper.writeValueAsString(request),
                request.createdAtWithOffset(),
                transmissionTime,
                retryCount
        );
        return webhookLogRepository.save(webhookLog);
    }

    @Transactional
    public void updateRetryCount(String webhookId, int retryCount) {
        WebhookLog webhookLog = findWebhookLog(webhookId);
        webhookLog.updateRetryCount(retryCount);
    }

    @Transactional
    public void markProcessing(String webhookId) {
        WebhookLog webhookLog = findWebhookLog(webhookId);

        if (!webhookLog.canProcess()) {
            throw new IllegalStateException("이미 처리 중이거나 완료된 웹훅: " + webhookId);
        }

        webhookLog.markProcessing();
        log.info("웹훅 처리 시작: webhookId={}, status={}", webhookId, webhookLog.getStatus());
    }

    @Transactional
    public void markSuccess(String webhookId) {
        WebhookLog webhookLog = findWebhookLog(webhookId);

        webhookLog.markSuccess();
        log.info("웹훅 처리 완료: webhookId={}, status={}", webhookId, webhookLog.getStatus());
    }


    @Transactional
    public void markFailed(String webhookId, String errorMessage) {
        WebhookLog webhookLog = findWebhookLog(webhookId);

        webhookLog.markFailed(errorMessage);
        log.error("웹훅 처리 실패: webhookId={}, status={}, error={}",
                webhookId, webhookLog.getStatus(), errorMessage);
    }

    private WebhookLog findWebhookLog(String webhookId) {
        return webhookLogRepository.findByWebhookId(webhookId)
                .orElseThrow(() -> new IllegalStateException("WebhookLog를 찾을 수 없음: " + webhookId));
    }
}
