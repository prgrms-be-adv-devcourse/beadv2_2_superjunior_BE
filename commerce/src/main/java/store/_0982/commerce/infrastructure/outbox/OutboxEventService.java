package store._0982.commerce.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.dto.BaseEvent;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private volatile boolean dbInfoLogged = false;

    public void record(String topic,
                       String messageKey,
                       BaseEvent payload,
                       String aggregateType,
                       String aggregateId){
        log.info("[OUTBOX][RECORD] topic={}, key={}, eventType={}, aggregateType={}, aggregateId={}",
                topic, messageKey, payload.getEventType(), aggregateType, aggregateId);
        OutboxEvent event = OutboxEvent.builder()
                .id(payload.getEventId())
                .topic(topic)
                .messageKey(messageKey)
                .payloadType(payload.getClass().getName())
                .payload(serialize(payload))
                .status(OutboxStatus.PENDING)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(payload.getEventType())
                .retryCount(0)
                .createdAt(OffsetDateTime.now())
                .nextAttemptAt(OffsetDateTime.now())
                .build();
        outboxEventRepository.save(event);
    }

    @Transactional
    public List<OutboxEvent> claimPending(int limit, Duration processingTimeout){
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime staleBefore = now.minus(processingTimeout);

        logDbInfoOnce();
        log.info("[OUTBOX][CLAIM] now={}, staleBefore={}, limit={}", now, staleBefore, limit);
        try {
            Integer pendingCount = jdbcTemplate.queryForObject(
                    """
                    SELECT count(*)
                    FROM product_schema.outbox_event
                    WHERE (status = 'PENDING' AND next_attempt_at <= ?)
                       OR (status = 'PROCESSING' AND processing_started_at <= ?)
                    """,
                    Integer.class,
                    now,
                    staleBefore
            );
            log.info("[OUTBOX][CLAIM] candidateCount={}", pendingCount);
        } catch (Exception e) {
            log.warn("[OUTBOX][CLAIM] failed to count candidates", e);
        }
        List<OutboxEvent> events = outboxEventRepository.findPendingForUpdate(now, staleBefore, limit);
        log.info("[OUTBOX][CLAIM] fetched={}", events.size());
        for(OutboxEvent event : events){
            event.markProcessing(now);
        }
        return events;
    }

    @Transactional
    public void markSent(UUID id){
        OutboxEvent event = outboxEventRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.OUTBOX_NOT_FOUND));
        event.markSent(OffsetDateTime.now());
    }

    @Transactional
    public void markRetry(UUID id, String error, OffsetDateTime nextAttemptAt, int maxRetries) {
        OutboxEvent event = outboxEventRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.OUTBOX_NOT_FOUND));

        if (event.getRetryCount() + 1 >= maxRetries) {
            event.markFailed(error);
            return;
        }
        event.markRetry(nextAttemptAt, error);
    }

    @Transactional
    public void markFailed(UUID id, String error) {
        OutboxEvent event = outboxEventRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.OUTBOX_NOT_FOUND));
        event.markFailed(error);
    }

    private String serialize(BaseEvent payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }

    private void logDbInfoOnce() {
        if (dbInfoLogged) {
            return;
        }
        try {
            String db = jdbcTemplate.queryForObject("select current_database()", String.class);
            String schema = jdbcTemplate.queryForObject("select current_schema()", String.class);
            String searchPath = jdbcTemplate.queryForObject("show search_path", String.class);
            log.info("[OUTBOX][DB] database={}, schema={}, search_path={}", db, schema, searchPath);
        } catch (Exception e) {
            log.warn("[OUTBOX][DB] failed to read db info", e);
        } finally {
            dbInfoLogged = true;
        }
    }
}
