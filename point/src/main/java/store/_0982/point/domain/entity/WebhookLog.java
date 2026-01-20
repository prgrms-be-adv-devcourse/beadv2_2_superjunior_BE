package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.point.domain.constant.WebhookStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "webhook_log", schema = "payment_schema")
public class WebhookLog {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "webhook_id", nullable = false, unique = true, updatable = false)
    private String webhookId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WebhookStatus status;

    @Column(name = "retry_count", nullable = false)
    @ColumnDefault("0")
    private int retryCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // 웹훅 이벤트가 생성된 시각 (PG 서버에서 보내줌)
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    // 웹훅 이벤트가 발송된 시각 (재시도마다 값이 바뀜)
    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @CreationTimestamp
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public static WebhookLog create(String webhookId, String eventType, String payload,
                                    OffsetDateTime occurredAt, OffsetDateTime sentAt, int retryCount) {
        return WebhookLog.builder()
                .webhookId(webhookId)
                .eventType(eventType)
                .payload(payload)
                .status(WebhookStatus.PENDING)
                .retryCount(retryCount)
                .occurredAt(occurredAt)
                .sentAt(sentAt)
                .build();
    }

    public void markProcessing() {
        this.status = WebhookStatus.PROCESSING;
    }

    public void markSuccess() {
        this.status = WebhookStatus.SUCCESS;
    }

    public void markFailed(String errorMessage) {
        this.status = WebhookStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void updateRetryCount(int retryCount) {
        this.retryCount = Math.max(this.retryCount, retryCount);
    }

    public boolean isAlreadyProcessed() {
        return status == WebhookStatus.SUCCESS || status == WebhookStatus.PROCESSING;
    }

    public boolean canProcess() {
        return status == WebhookStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
