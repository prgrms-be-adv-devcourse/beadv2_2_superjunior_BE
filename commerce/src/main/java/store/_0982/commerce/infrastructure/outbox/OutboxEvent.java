package store._0982.commerce.infrastructure.outbox;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "outbox_event", schema = "product_schema",indexes = {
        @Index(name = "idx_outbox_status_next_attempt", columnList = "status, next_attempt_at, created_at"),
        @Index(name = "idx_outbox_processing_started", columnList = "status, processing_started_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OutboxEvent {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String topic;

    @Column(name = "message_key", nullable = false)
    private String messageKey;

    @Column(name = "payload_type", nullable = false)
    private String payloadType;

    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(name = "aggregate_type")
    private String aggregateType;

    @Column(name = "aggregate_id")
    private String aggregateId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "next_attempt_at", nullable = false)
    private OffsetDateTime nextAttemptAt;

    @Column(name = "processing_started_at")
    private OffsetDateTime processingStartedAt;

    @Column(name = "last_error")
    private String lastError;

    public void markProcessing(OffsetDateTime now) {
        this.status = OutboxStatus.PROCESSING;
        this.processingStartedAt = now;
    }

    public void markSent(OffsetDateTime now) {
        this.status = OutboxStatus.SENT;
        this.publishedAt = now;
        this.lastError = null;
    }

    public void markRetry(OffsetDateTime nextAttemptAt, String error) {
        this.status = OutboxStatus.PENDING;
        this.retryCount += 1;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = error;
        this.processingStartedAt = null;
    }

    public void markFailed(String error) {
        this.status = OutboxStatus.FAILED;
        this.lastError = error;
        this.processingStartedAt = null;
    }

}
