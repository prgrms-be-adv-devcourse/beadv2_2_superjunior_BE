package store._0982.notification.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.notification.domain.Notification;
import store._0982.notification.domain.NotificationRepository;
import store._0982.notification.domain.NotificationStatus;
import store._0982.notification.exception.CustomErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("알림을 읽음 처리한다")
    void read_success() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        Notification notification = Notification.builder()
                .id(notificationId)
                .memberId(memberId)
                .status(NotificationStatus.SENT)
                .build();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when
        notificationService.read(memberId, notificationId);

        // then
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);
        verify(notificationRepository).findById(notificationId);
    }

    @Test
    @DisplayName("존재하지 않는 알림을 읽으려고 하면 예외가 발생한다")
    void read_fail_whenNotificationNotFound() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.read(memberId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("다른 회원의 알림을 읽으려고 하면 예외가 발생한다")
    void read_fail_whenNotOwnNotification() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID otherMemberId = UUID.randomUUID();

        Notification notification = Notification.builder()
                .id(notificationId)
                .memberId(otherMemberId)
                .status(NotificationStatus.SENT)
                .build();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.read(memberId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.NO_PERMISSION_TO_READ.getMessage());
    }

    @Test
    @DisplayName("읽지 않은 알림을 모두 읽음 처리한다")
    void readAll_success() {
        // given
        UUID memberId = UUID.randomUUID();

        Notification notification1 = Notification.builder()
                .id(UUID.randomUUID())
                .memberId(memberId)
                .status(NotificationStatus.SENT)
                .build();

        Notification notification2 = Notification.builder()
                .id(UUID.randomUUID())
                .memberId(memberId)
                .status(NotificationStatus.SENT)
                .build();

        when(notificationRepository.findByMemberIdAndStatus(memberId, NotificationStatus.SENT))
                .thenReturn(java.util.List.of(notification1, notification2));

        // when
        notificationService.readAll(memberId);

        // then
        assertThat(notification1.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(notification2.getStatus()).isEqualTo(NotificationStatus.READ);
        verify(notificationRepository).findByMemberIdAndStatus(memberId, NotificationStatus.SENT);
    }
}
