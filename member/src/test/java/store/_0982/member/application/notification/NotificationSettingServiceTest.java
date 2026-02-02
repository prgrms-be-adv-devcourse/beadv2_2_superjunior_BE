package store._0982.member.application.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.member.application.notification.dto.NotificationSettingInfo;
import store._0982.member.application.notification.dto.NotificationSettingUpdateCommand;
import store._0982.member.domain.notification.NotificationSetting;
import store._0982.member.domain.notification.NotificationSettingRepository;
import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

    private static final int CHANNEL_COUNTS = NotificationChannel.values().length;

    @InjectMocks
    private NotificationSettingService notificationSettingService;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
    }

    @Test
    @DisplayName("회원 가입 시 초기 설정을 생성한다")
    void initializeSettings() {
        // when
        notificationSettingService.initializeSettings(memberId);

        // then
        verify(notificationSettingRepository).saveAll(argThat(settings ->
                settings.size() == CHANNEL_COUNTS && settings.stream().allMatch(NotificationSetting::isEnabled)));
    }

    @Test
    @DisplayName("모든 알림 설정을 조회한다 (DB에 값이 없는 경우 기본값 true 반환)")
    void getAllSettings_whenNoDataInDB() {
        // given
        when(notificationSettingRepository.findAllByMemberId(memberId)).thenReturn(Collections.emptyList());

        // when
        List<NotificationSettingInfo> result = notificationSettingService.getAllSettings(memberId);

        // then
        assertThat(result)
                .hasSize(CHANNEL_COUNTS)
                .allMatch(NotificationSettingInfo::isEnabled);
    }

    @Test
    @DisplayName("모든 알림 설정을 조회한다 (DB에 값이 있고, 기본 채널은 강제 true 반환)")
    void getAllSettings_whenDataExists() {
        // given
        // EMAIL은 false로 저장되어 있다고 가정
        NotificationSetting emailSetting = NotificationSetting.create(memberId, NotificationChannel.EMAIL, false);
        // IN_APP은 false로 저장되어 있어도 조회 시 true여야 함 (엣지 케이스: DB가 오염된 상황 가정)
        NotificationSetting inAppSetting = NotificationSetting.create(memberId, NotificationChannel.IN_APP, false);

        when(notificationSettingRepository.findAllByMemberId(memberId)).thenReturn(List.of(emailSetting, inAppSetting));

        // when
        List<NotificationSettingInfo> result = notificationSettingService.getAllSettings(memberId);

        // then
        assertThat(result)
                .hasSize(CHANNEL_COUNTS)
                .extracting(NotificationSettingInfo::channel, NotificationSettingInfo::isEnabled)
                .containsExactly(tuple(NotificationChannel.EMAIL, false), tuple(NotificationChannel.IN_APP, true));
    }

    @Test
    @DisplayName("설정을 업데이트한다 (기존 설정이 있는 경우)")
    void updateSettings_existing() {
        // given
        NotificationSetting emailSetting = mock(NotificationSetting.class);
        when(emailSetting.getChannel()).thenReturn(NotificationChannel.EMAIL);

        when(notificationSettingRepository.findAllByMemberId(memberId)).thenReturn(List.of(emailSetting));

        List<NotificationSettingUpdateCommand> commands = List.of(
                new NotificationSettingUpdateCommand(NotificationChannel.EMAIL, false)
        );

        // when
        notificationSettingService.updateSettings(memberId, commands);

        // then
        verify(emailSetting).update(false);
        verify(notificationSettingRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("설정을 업데이트한다 (기존 설정이 없는 경우 새로 생성)")
    void updateSettings_new() {
        // given
        when(notificationSettingRepository.findAllByMemberId(memberId)).thenReturn(Collections.emptyList());

        List<NotificationSettingUpdateCommand> commands = List.of(
                new NotificationSettingUpdateCommand(NotificationChannel.EMAIL, false)
        );

        // when
        notificationSettingService.updateSettings(memberId, commands);

        // then
        verify(notificationSettingRepository).saveAll(argThat(settings -> {
            List<NotificationSetting> list = (List<NotificationSetting>) settings;
            NotificationSetting setting = list.get(0);
            return setting.getChannel() == NotificationChannel.EMAIL && !setting.isEnabled();
        }));
    }

    @Test
    @DisplayName("기본 채널(IN_APP)은 false로 업데이트 요청이 와도 무시하고 true로 저장한다")
    void updateSettings_defaultChannel_forceTrue() {
        // given
        NotificationSetting inAppSetting = mock(NotificationSetting.class);
        when(inAppSetting.getChannel()).thenReturn(NotificationChannel.IN_APP);

        when(notificationSettingRepository.findAllByMemberId(memberId)).thenReturn(List.of(inAppSetting));

        // false로 끄기 요청
        List<NotificationSettingUpdateCommand> commands = List.of(
                new NotificationSettingUpdateCommand(NotificationChannel.IN_APP, false)
        );

        // when
        notificationSettingService.updateSettings(memberId, commands);

        // then
        verify(inAppSetting).update(true); // false 요청을 무시하고 true로 업데이트
        verify(notificationSettingRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("기본 채널(IN_APP)이 DB에 없는 상태에서 false로 업데이트 요청이 와도 true로 생성한다")
    void updateSettings_defaultChannel_createForceTrue() {
        // given
        when(notificationSettingRepository.findAllByMemberId(memberId)).thenReturn(Collections.emptyList());

        List<NotificationSettingUpdateCommand> commands = List.of(
                new NotificationSettingUpdateCommand(NotificationChannel.IN_APP, false)
        );

        // when
        notificationSettingService.updateSettings(memberId, commands);

        // then
        verify(notificationSettingRepository).saveAll(argThat(settings -> {
            List<NotificationSetting> list = (List<NotificationSetting>) settings;
            NotificationSetting setting = list.get(0);
            return setting.getChannel() == NotificationChannel.IN_APP && setting.isEnabled(); // true여야 함
        }));
    }
}
