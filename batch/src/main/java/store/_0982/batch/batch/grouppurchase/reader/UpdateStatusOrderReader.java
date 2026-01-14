package store._0982.batch.batch.grouppurchase.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.policy.GroupPurchasePolicy;
import store._0982.batch.domain.order.Order;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 공동 구매 주문 건 상태 변경 대상 Reader
 */
@Component
@RequiredArgsConstructor
public class UpdateStatusOrderReader {

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope
    public JpaPagingItemReader<Order> create(
            @Value("#{stepExecutionContext['processedGroupPurchaseIds']}")List<UUID> groupPurchaseIds
            ) {
        return new JpaPagingItemReaderBuilder<Order>()
                .name("updateStatusOrderReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        """
                            SELECT o
                            FROM Order o
                            WHERE o.groupPurchaseId IN :groupPurchaseIds
                        """
                ).parameterValues(Map.of("groupPurchaseIds", groupPurchaseIds))
                .pageSize(GroupPurchasePolicy.Order.CHUNK_UNIT)
                .build();
    }
}
