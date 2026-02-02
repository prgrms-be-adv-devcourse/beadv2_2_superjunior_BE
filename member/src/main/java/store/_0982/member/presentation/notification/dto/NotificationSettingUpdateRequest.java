package store._0982.member.presentation.notification.dto;

import jakarta.validation.constraints.NotNull;
import store._0982.member.domain.notification.constant.NotificationChannel;

public record NotificationSettingUpdateRequest(
        @NotNull NotificationChannel channel,
        boolean isEnabled
) {
}
