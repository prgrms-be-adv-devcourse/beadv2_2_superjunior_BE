package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import store._0982.point.domain.entity.WebhookLog;
import store._0982.point.domain.repository.WebhookLogRepository;
import store._0982.point.presentation.dto.TossWebhookRequest;

import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookLogService {

    private final WebhookLogRepository webhookLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<WebhookLog> getUnfinishedWebhook(String webhookId, OffsetDateTime transmissionTime,
                                                     TossWebhookRequest request, int retryCount) throws JsonProcessingException {
        Optional<WebhookLog> optionalWebhookLog = webhookLogRepository.findByWebhookIdWithLock(webhookId);
        if (optionalWebhookLog.isEmpty()) {
            return Optional.of(createWebhookLog(webhookId, transmissionTime, request, retryCount));
        }

        WebhookLog webhookLog = optionalWebhookLog.get();
        if (webhookLog.isAlreadyProcessed()) {
            return Optional.empty();
        }

        webhookLog.update(retryCount, transmissionTime, objectMapper.writeValueAsString(request));
        return Optional.of(webhookLog);
    }

    private WebhookLog createWebhookLog(String webhookId, OffsetDateTime transmissionTime,
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
}
