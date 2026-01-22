package store._0982.member.application.notification;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.member.application.notification.dto.NotificationSettingInfo;
import store._0982.member.application.notification.dto.NotificationSettingUpdateCommand;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.NotificationSetting;
import store._0982.member.domain.notification.NotificationSettingRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional
    public void updateSettings(UUID memberId, List<NotificationSettingUpdateCommand> commands) {
        Map<NotificationChannel, NotificationSetting> currentSettings = getNotificationSettings(memberId);

        List<NotificationSetting> updatedSettings = commands.stream()
                .map(command -> {
                    NotificationChannel channel = command.channel();
                    boolean isEnabled = command.isEnabled() || channel.isDefaultChannel();

                    if (currentSettings.containsKey(channel)) {
                        NotificationSetting setting = currentSettings.get(channel);
                        setting.update(isEnabled);
                        return setting;
                    }
                    return NotificationSetting.create(memberId, channel, isEnabled);
                })
                .toList();

        notificationSettingRepository.saveAll(updatedSettings);
    }

    /**
     * 회원 가입 시 호출되는 메서드입니다.
     */
    @Transactional
    public void initializeSettings(UUID memberId) {
        List<NotificationSetting> settings = Arrays.stream(NotificationChannel.values())
                .map(channel -> NotificationSetting.create(memberId, channel, true))
                .toList();
        notificationSettingRepository.saveAll(settings);
    }

    public List<NotificationSettingInfo> getAllSettings(UUID memberId) {
        Map<NotificationChannel, NotificationSetting> storedSettings = getNotificationSettings(memberId);

        return Arrays.stream(NotificationChannel.values())
                .map(channel -> {
                    if (storedSettings.containsKey(channel) && !channel.isDefaultChannel()) {
                        // 기본 채널을 제외한 나머지 채널은 설정값대로 표시됨
                        return NotificationSettingInfo.of(channel, storedSettings.get(channel).isEnabled());
                    }
                    // DB에 설정값이 없거나 기본 채널인 경우는 true로 표시됨
                    return NotificationSettingInfo.of(channel, true);
                })
                .toList();
    }

    public boolean isEnabled(UUID memberId, NotificationChannel channel) {
        if (channel.isDefaultChannel()) {
            return true;
        }
        return getNotificationSettings(memberId).get(channel).isEnabled();
    }

    private @NonNull Map<NotificationChannel, NotificationSetting> getNotificationSettings(UUID memberId) {
        return notificationSettingRepository.findAllByMemberId(memberId)
                .stream()
                .collect(Collectors.toMap(NotificationSetting::getChannel, Function.identity()));
    }
}
