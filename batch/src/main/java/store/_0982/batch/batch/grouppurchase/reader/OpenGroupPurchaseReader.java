package store._0982.batch.batch.grouppurchase.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseProjection;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 공동구매 시작 대상 Reader
 */
@Configuration
@RequiredArgsConstructor
public class OpenGroupPurchaseReader {

    private final EntityManagerFactory entityManagerFactory;

    @StepScope
    @Bean
    public JpaCursorItemReader<GroupPurchaseProjection> openGroupPurchase(
            @Value("#{jobParameters['now']}") String now
    ) {
        OffsetDateTime parsedNow = OffsetDateTime.parse(now);
        return new JpaCursorItemReaderBuilder<GroupPurchaseProjection>()
                .name("openGroupPurchaseReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT new store._0982.batch.batch.grouppurchase.dto.GroupPurchaseProjection(" +
                        "g.groupPurchaseId, g.status, g.currentQuantity, g.minQuantity, " +
                        "g.sellerId, g.productId, g.title, g.description, g.discountedPrice, " +
                        "g.endDate, g.updatedAt, p.price, p.category) " +
                        "FROM GroupPurchase g, Product p " +
                        "WHERE p.productId = g.productId " +
                        "AND g.status = :status " +
                        "AND g.startDate <= :now"
                )
                .parameterValues(Map.of(
                        "status", GroupPurchaseStatus.SCHEDULED,
                        "now", parsedNow
                ))
                .build();
    }
}
