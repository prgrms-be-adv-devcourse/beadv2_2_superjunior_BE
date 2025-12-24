package store._0982.member.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.common.exception.CustomException;
import store._0982.member.exception.CustomErrorCode;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class NotificationTest {
    @Test
    @DisplayName("알림을 읽음 상태로 변경할 수 있다")
    void read_success() {
        Notification notification = createSample(UUID.randomUUID(), NotificationStatus.SENT);

        notification.read();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);
    }

    @Test
    @DisplayName("실패 상태의 알림은 읽을 수 없다")
    void read_fail() {
        Notification notification = createSample(UUID.randomUUID(), NotificationStatus.FAILED);

        assertThatThrownBy(notification::read)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.CANNOT_READ.getMessage());
    }

    @Test
    @DisplayName("다른 회원의 알림은 검증에 실패한다")
    void validateMemberId_fail() {
        // given
        UUID memberId = UUID.randomUUID();
        Notification notification = createSample(memberId, NotificationStatus.SENT);

        // when & then
        assertThatThrownBy(() -> notification.validateMemberId(UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.NO_PERMISSION_TO_READ.getMessage());
    }

    @Test
    @DisplayName("같은 회원의 알림은 검증에 성공한다")
    void validateMemberId_success() {
        // given
        UUID memberId = UUID.randomUUID();
        Notification notification = createSample(memberId, NotificationStatus.SENT);

        // when & then
        notification.validateMemberId(memberId);
    }

    @Test
    @DisplayName("ID가 없는 알림을 생성하면 PrePersist에서 UUID가 자동 생성된다")
    void prePersist_generatesId() {
        // given
        Notification notification = Notification.builder()
                .memberId(UUID.randomUUID())
                .type(NotificationType.POINT_RECHARGED)
                .channel(NotificationChannel.IN_APP)
                .title("테스트")
                .message("테스트 메시지")
                .referenceType(ReferenceType.POINT)
                .referenceId(UUID.randomUUID())
                .status(NotificationStatus.SENT)
                .build();

        // when
        notification.onCreate();

        // then
        assertThat(notification.getId()).isNotNull();
    }

    @Test
    @DisplayName("ID가 이미 있는 알림은 PrePersist에서 ID가 변경되지 않는다")
    void prePersist_keepsExistingId() {
        // given
        UUID existingId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(existingId)
                .memberId(UUID.randomUUID())
                .type(NotificationType.POINT_RECHARGED)
                .channel(NotificationChannel.IN_APP)
                .title("테스트")
                .message("테스트 메시지")
                .referenceType(ReferenceType.POINT)
                .referenceId(UUID.randomUUID())
                .status(NotificationStatus.SENT)
                .build();

        // when
        notification.onCreate();

        // then
        assertThat(notification.getId()).isEqualTo(existingId);
    }

    private Notification createSample(UUID memberId, NotificationStatus status) {
        return Notification.builder()
                .id(UUID.randomUUID())
                .memberId(memberId)
                .type(NotificationType.GROUP_PURCHASE_COMPLETED)
                .channel(NotificationChannel.IN_APP)
                .title("포인트 충전")
                .message("1000원 충전")
                .referenceType(ReferenceType.POINT)
                .referenceId(UUID.randomUUID())
                .status(status)
                .build();
    }
}
