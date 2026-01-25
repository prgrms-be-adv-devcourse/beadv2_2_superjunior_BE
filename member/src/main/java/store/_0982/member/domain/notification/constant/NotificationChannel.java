package store._0982.member.domain.notification.constant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationChannel {
    EMAIL(false),
    IN_APP(true);

    private final boolean isDefault;

    public boolean isDefaultChannel() {
        return this.isDefault;
    }
}
