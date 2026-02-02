package store._0982.member.application.notification.dispatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.member.application.notification.BulkNotifiable;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.application.notification.NotificationService;
import store._0982.member.application.notification.NotificationSettingService;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InAppDispatchServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationSettingService notificationSettingService;

    @InjectMocks
    private InAppDispatchService inAppDispatchService;

    @Test
    @DisplayName("단일 알림: 알림 설정이 활성화된 경우 알림을 저장한다")
    void notifyToInApp_single_success_whenEnabled() {
        // given
        UUID memberId = UUID.randomUUID();
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "주문 완료",
                "주문이 완료되었습니다",
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.IN_APP))
                .thenReturn(true);

        // when
        inAppDispatchService.notifyToInApp(notifiable);

        // then
        verify(notificationSettingService).isEnabled(memberId, NotificationChannel.IN_APP);
        verify(notificationService).saveNotification(notifiable, NotificationChannel.IN_APP);
    }

    @Test
    @DisplayName("단일 알림: 알림 설정이 비활성화된 경우 알림을 저장하지 않는다")
    void notifyToInApp_single_skip_whenDisabled() {
        // given
        UUID memberId = UUID.randomUUID();
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "주문 완료",
                "주문이 완료되었습니다",
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.IN_APP))
                .thenReturn(false);

        // when
        inAppDispatchService.notifyToInApp(notifiable);

        // then
        verify(notificationSettingService).isEnabled(memberId, NotificationChannel.IN_APP);
        verify(notificationService, never()).saveNotification(any(), any());
    }

    @Test
    @DisplayName("대량 알림: 모든 사용자의 알림 설정이 활성화된 경우 모든 알림을 저장한다")
    void notifyToInApp_bulk_success_whenAllEnabled() {
        // given
        UUID memberId1 = UUID.randomUUID();
        UUID memberId2 = UUID.randomUUID();
        UUID memberId3 = UUID.randomUUID();

        NotificationContent content = new NotificationContent(
                NotificationType.GROUP_PURCHASE_SUCCESS,
                "공동 구매 성공",
                "공동 구매가 성공했습니다",
                UUID.randomUUID()
        );

        List<Notifiable> notifiables = List.of(
                Notifiable.of(memberId1, content),
                Notifiable.of(memberId2, content),
                Notifiable.of(memberId3, content)
        );

        BulkNotifiable bulkNotifiable = () -> notifiables;

        when(notificationSettingService.isEnabled(memberId1, NotificationChannel.IN_APP))
                .thenReturn(true);
        when(notificationSettingService.isEnabled(memberId2, NotificationChannel.IN_APP))
                .thenReturn(true);
        when(notificationSettingService.isEnabled(memberId3, NotificationChannel.IN_APP))
                .thenReturn(true);

        // when
        inAppDispatchService.notifyToInApp(bulkNotifiable);

        // then
        verify(notificationSettingService, times(3)).isEnabled(any(), eq(NotificationChannel.IN_APP));
        verify(notificationService).saveBulkNotifications(notifiables, NotificationChannel.IN_APP);
    }

    @Test
    @DisplayName("대량 알림: 일부 사용자만 알림 설정이 활성화된 경우 활성화된 사용자의 알림만 저장한다")
    void notifyToInApp_bulk_partial_whenSomeEnabled() {
        // given
        UUID memberId1 = UUID.randomUUID();
        UUID memberId2 = UUID.randomUUID();
        UUID memberId3 = UUID.randomUUID();

        NotificationContent content = new NotificationContent(
                NotificationType.GROUP_PURCHASE_SUCCESS,
                "공동 구매 성공",
                "공동 구매가 성공했습니다",
                UUID.randomUUID()
        );

        Notifiable notifiable1 = Notifiable.of(memberId1, content);
        Notifiable notifiable2 = Notifiable.of(memberId2, content);
        Notifiable notifiable3 = Notifiable.of(memberId3, content);

        List<Notifiable> allNotifiables = List.of(notifiable1, notifiable2, notifiable3);
        BulkNotifiable bulkNotifiable = () -> allNotifiables;

        when(notificationSettingService.isEnabled(memberId1, NotificationChannel.IN_APP))
                .thenReturn(true);
        when(notificationSettingService.isEnabled(memberId2, NotificationChannel.IN_APP))
                .thenReturn(false);
        when(notificationSettingService.isEnabled(memberId3, NotificationChannel.IN_APP))
                .thenReturn(true);

        // when
        inAppDispatchService.notifyToInApp(bulkNotifiable);

        // then
        verify(notificationSettingService, times(3)).isEnabled(any(), eq(NotificationChannel.IN_APP));
        verify(notificationService).saveBulkNotifications(
                argThat(list -> list.size() == 2 && list.contains(notifiable1) && list.contains(notifiable3)),
                eq(NotificationChannel.IN_APP)
        );
    }

    @Test
    @DisplayName("대량 알림: 모든 사용자의 알림 설정이 비활성화된 경우 알림을 저장하지 않는다")
    void notifyToInApp_bulk_skip_whenAllDisabled() {
        // given
        UUID memberId1 = UUID.randomUUID();
        UUID memberId2 = UUID.randomUUID();

        NotificationContent content = new NotificationContent(
                NotificationType.GROUP_PURCHASE_SUCCESS,
                "공동 구매 성공",
                "공동 구매가 성공했습니다",
                UUID.randomUUID()
        );

        List<Notifiable> notifiables = List.of(
                Notifiable.of(memberId1, content),
                Notifiable.of(memberId2, content)
        );

        BulkNotifiable bulkNotifiable = () -> notifiables;

        when(notificationSettingService.isEnabled(memberId1, NotificationChannel.IN_APP))
                .thenReturn(false);
        when(notificationSettingService.isEnabled(memberId2, NotificationChannel.IN_APP))
                .thenReturn(false);

        // when
        inAppDispatchService.notifyToInApp(bulkNotifiable);

        // then
        verify(notificationSettingService, times(2)).isEnabled(any(), eq(NotificationChannel.IN_APP));
        verify(notificationService, never()).saveBulkNotifications(any(), any());
    }

    @Test
    @DisplayName("대량 알림: notifiables가 빈 리스트인 경우 알림을 저장하지 않는다")
    void notifyToInApp_bulk_skip_whenEmptyList() {
        // given
        BulkNotifiable bulkNotifiable = Collections::emptyList;

        // when
        inAppDispatchService.notifyToInApp(bulkNotifiable);

        // then
        verify(notificationSettingService, never()).isEnabled(any(), any());
        verify(notificationService, never()).saveBulkNotifications(any(), any());
    }

    @Test
    @DisplayName("대량 알림: 단일 사용자만 포함된 경우에도 정상적으로 처리한다")
    void notifyToInApp_bulk_success_withSingleUser() {
        // given
        UUID memberId = UUID.randomUUID();
        NotificationContent content = new NotificationContent(
                NotificationType.GROUP_PURCHASE_SUCCESS,
                "공동 구매 성공",
                "공동 구매가 성공했습니다",
                UUID.randomUUID()
        );

        List<Notifiable> notifiables = List.of(Notifiable.of(memberId, content));
        BulkNotifiable bulkNotifiable = () -> notifiables;

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.IN_APP))
                .thenReturn(true);

        // when
        inAppDispatchService.notifyToInApp(bulkNotifiable);

        // then
        verify(notificationSettingService).isEnabled(memberId, NotificationChannel.IN_APP);
        verify(notificationService).saveBulkNotifications(notifiables, NotificationChannel.IN_APP);
    }

    @Test
    @DisplayName("대량 알림: 대량의 사용자에게도 정상적으로 처리한다")
    void notifyToInApp_bulk_success_withManyUsers() {
        // given
        List<Notifiable> notifiables = new ArrayList<>();
        NotificationContent content = new NotificationContent(
                NotificationType.GROUP_PURCHASE_SUCCESS,
                "공동 구매 성공",
                "공동 구매가 성공했습니다",
                UUID.randomUUID()
        );

        for (int i = 0; i < 100; i++) {
            UUID memberId = UUID.randomUUID();
            notifiables.add(Notifiable.of(memberId, content));
            when(notificationSettingService.isEnabled(memberId, NotificationChannel.IN_APP))
                    .thenReturn(true);
        }

        BulkNotifiable bulkNotifiable = () -> notifiables;

        // when
        inAppDispatchService.notifyToInApp(bulkNotifiable);

        // then
        verify(notificationSettingService, times(100)).isEnabled(any(), eq(NotificationChannel.IN_APP));
        verify(notificationService).saveBulkNotifications(
                argThat(list -> list.size() == 100),
                eq(NotificationChannel.IN_APP)
        );
    }
}
