package store._0982.member.domain.notification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.exception.CustomException;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.constant.NotificationStatus;
import store._0982.member.domain.notification.constant.NotificationType;
import store._0982.member.domain.notification.constant.ReferenceType;
import store._0982.member.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "notification",
        schema = "notification_schema",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_deduplication",
                        columnNames = {"reference_id", "notification_type", "member_id"}
                )
        }
)
public class Notification {

    @Id
    @Column(name = "notification_id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "failure_message", columnDefinition = "TEXT")
    private String failureMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    public static Notification from(NotificationContent content, NotificationChannel channel, UUID memberId) {
        return Notification.builder()
                .memberId(memberId)
                .channel(channel)
                .referenceId(content.referenceId())
                .type(content.type())
                .title(content.title())
                .message(content.message())
                .status(NotificationStatus.SENT)
                .build();
    }

    public void validateMemberId(UUID memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new CustomException(CustomErrorCode.NO_PERMISSION_TO_READ);
        }
    }

    public void read() {
        if (this.status == NotificationStatus.FAILED) {
            throw new CustomException(CustomErrorCode.CANNOT_READ);
        }
        this.status = NotificationStatus.READ;
    }

    public ReferenceType getReferenceType() {
        return type.getReferenceType();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
