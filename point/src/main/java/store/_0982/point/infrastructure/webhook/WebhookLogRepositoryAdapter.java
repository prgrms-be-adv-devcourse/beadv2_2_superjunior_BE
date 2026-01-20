package store._0982.point.infrastructure.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.WebhookLog;
import store._0982.point.domain.repository.WebhookLogRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WebhookLogRepositoryAdapter implements WebhookLogRepository {

    private final WebhookLogJpaRepository webhookLogJpaRepository;

    @Override
    public WebhookLog save(WebhookLog webhookLog) {
        return webhookLogJpaRepository.save(webhookLog);
    }

    @Override
    public Optional<WebhookLog> findByWebhookId(String webhookId) {
        return webhookLogJpaRepository.findByWebhookId(webhookId);
    }
}
