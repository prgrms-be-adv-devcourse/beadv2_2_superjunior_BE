package store._0982.member.application.notification.dto;

import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.presentation.notification.dto.NotificationSettingUpdateRequest;

public record NotificationSettingUpdateCommand(
        NotificationChannel channel,
        boolean isEnabled
) {
    public static NotificationSettingUpdateCommand from(NotificationSettingUpdateRequest request) {
        return new NotificationSettingUpdateCommand(request.channel(), request.isEnabled());
    }
}
