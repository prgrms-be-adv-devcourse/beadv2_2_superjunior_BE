package store._0982.commerce.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.dto.BaseEvent;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private final OutboxEventService outboxEventService;
    private final KafkaTemplate<String, BaseEvent> outboxKafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${outbox.publisher.batch-size:100}")
    private int batchSize;

    @Value("${outbox.publisher.send-timeout-ms:5000}")
    private long sendTimeoutMs;

    @Value("${outbox.publisher.processing-timeout-ms:600000}")
    private long processingTimeoutMs;

    @Value("${outbox.publisher.retry-base-delay-ms:2000}")
    private long retryBaseDelayMs;

    @Value("${outbox.publisher.retry-max-delay-ms:60000}")
    private long retryMaxDelayMs;

    @Value("${outbox.publisher.max-retries:10}")
    private int maxRetries;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:1000}")
    public void publish(){
        log.info("[OUTBOX][PUBLISH] tick");
        Duration processingTimeout = Duration.ofMillis(processingTimeoutMs);
        List<OutboxEvent> batch = outboxEventService.claimPending(batchSize, processingTimeout);

        if(batch.isEmpty()){
            log.debug("[OUTBOX][PUBLISH] no pending events");
            return;
        }

        log.info("[OUTBOX][PUBLISH] claimed {} events", batch.size());
        for(OutboxEvent event : batch){
            try{
                log.info("[OUTBOX][SEND] id={}, topic={}, key={}, retry={}",
                        event.getId(), event.getTopic(), event.getMessageKey(), event.getRetryCount());
                BaseEvent payload = deserialize(event);
                outboxKafkaTemplate.send(event.getTopic(), event.getMessageKey(), payload)
                        .get(sendTimeoutMs, TimeUnit.MILLISECONDS);
                outboxEventService.markSent(event.getId());
                log.info("[OUTBOX][SENT] id={}, topic={}", event.getId(), event.getTopic());
            } catch(Exception e){
                handleFailure(event, e);
            }
        }
    }

    private BaseEvent deserialize(OutboxEvent event) throws Exception{
        Class<?> type = Class.forName(event.getPayloadType());
        return (BaseEvent) objectMapper.readValue(event.getPayload(), type);
    }

    private void handleFailure(OutboxEvent event, Exception exception) {
        if (exception instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        String error = exception.getClass().getSimpleName() + ": " + exception.getMessage();
        OffsetDateTime nextAttemptAt = OffsetDateTime.now().plus(Duration.ofMillis(calculateBackoff(event.getRetryCount())));

        try {
            outboxEventService.markRetry(event.getId(), error, nextAttemptAt, maxRetries);
        } catch (Exception e) {
            log.error("[OUTBOX] Failed to update outbox status for {}", event.getId(), e);
        }

        log.warn("[OUTBOX] Publish failed (id={}, topic={}, retry={})", event.getId(), event.getTopic(), event.getRetryCount(), exception);
    }

    private long calculateBackoff(int retryCount) {
        long delay = retryBaseDelayMs * (long) Math.pow(2, Math.min(retryCount, 6));
        return Math.min(delay, retryMaxDelayMs);
    }

}
