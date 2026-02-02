package store._0982.point.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebhookHeaders {

    public static final String TOSS_TRANSMISSION_TIME = "tosspayments-webhook-transmission-time";
    public static final String TOSS_RETRY_COUNT = "tosspayments-webhook-transmission-retried-count";
    public static final String TOSS_WEBHOOK_ID = "tosspayments-webhook-transmission-id";

}
