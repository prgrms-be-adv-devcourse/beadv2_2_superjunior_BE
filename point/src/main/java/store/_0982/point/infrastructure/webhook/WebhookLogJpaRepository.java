package store._0982.point.infrastructure.webhook;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import store._0982.point.domain.entity.WebhookLog;

import java.util.Optional;
import java.util.UUID;

public interface WebhookLogJpaRepository extends JpaRepository<WebhookLog, UUID> {

    Optional<WebhookLog> findByWebhookId(String webhookId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WebhookLog> readByWebhookId(String webhookId);
}
