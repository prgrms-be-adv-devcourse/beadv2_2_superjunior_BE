package store._0982.member.domain.notification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.member.domain.notification.constant.NotificationChannel;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "notification_setting", schema = "notification_schema",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "channel"})
        }
)
public class NotificationSetting {

    @Id
    @Column(name = "setting_id")
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;        // 인앱 알림은 필수고, 그 외의 알림 채널은 설정 가능

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public static NotificationSetting create(UUID memberId, NotificationChannel channel) {
        return NotificationSetting.builder()
                .memberId(memberId)
                .channel(channel)
                .isEnabled(true)
                .build();
    }

    public void update(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
