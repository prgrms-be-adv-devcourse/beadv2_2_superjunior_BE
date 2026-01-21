package store._0982.member.application.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.member.application.notification.dto.NotificationSettingInfo;
import store._0982.member.application.notification.dto.NotificationSettingUpdateCommand;
import store._0982.member.presentation.notification.dto.NotificationSettingUpdateRequest;
import store._0982.member.domain.notification.NotificationSetting;
import store._0982.member.domain.notification.NotificationSettingRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional
    public void updateSetting(UUID memberId, NotificationSettingUpdateCommand command) {
        notificationSettingRepository.findByMemberIdAndChannel(
                memberId,
                command.channel()
        ).ifPresentOrElse(
                setting -> setting.update(command.isEnabled()),
                () -> notificationSettingRepository.save(
                        NotificationSetting.create(
                                memberId,
                                command.channel(),
                                command.isEnabled()
                        )
                )
        );
    }

    public NotificationSettingInfo getSetting(UUID memberId, NotificationSettingUpdateRequest request) {
        NotificationSetting setting = notificationSettingRepository.findByMemberIdAndChannel(
                memberId,
                request.channel()
        ).orElse(null);

        if (setting == null) {
            // 설정이 없으면 기본값 true 반환 (또는 정책에 따라 false)
            return NotificationSettingInfo.of(request.channel(), true);
        }

        return NotificationSettingInfo.from(setting);
    }
}
