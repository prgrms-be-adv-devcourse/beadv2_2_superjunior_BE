package store._0982.point.domain.repository;

import store._0982.point.domain.entity.WebhookLog;

import java.util.Optional;

public interface WebhookLogRepository {

    WebhookLog save(WebhookLog webhookLog);

    Optional<WebhookLog> findByWebhookId(String webhookId);
}
