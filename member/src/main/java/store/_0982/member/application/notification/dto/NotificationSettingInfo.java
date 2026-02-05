package store._0982.member.application.notification.dto;

import store._0982.member.domain.notification.NotificationSetting;
import store._0982.member.domain.notification.constant.NotificationChannel;

public record NotificationSettingInfo(
        NotificationChannel channel,
        boolean isEnabled
) {
    public static NotificationSettingInfo from(NotificationSetting setting) {
        return new NotificationSettingInfo(
                setting.getChannel(),
                setting.isEnabled()
        );
    }

    public static NotificationSettingInfo of(NotificationChannel channel, boolean isEnabled) {
        return new NotificationSettingInfo(channel, isEnabled);
    }
}
