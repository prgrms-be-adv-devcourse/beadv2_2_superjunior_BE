package store._0982.batch.batch.grouppurchase.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseWithProduct;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 공동구매 시작 대상 Reader
 */
@Configuration
@RequiredArgsConstructor
public class OpenGroupPurchaseReader {

    private final EntityManagerFactory entityManagerFactory;
    private final GroupPurchaseRepository groupPurchaseRepository;

    @StepScope
    @Bean
    public JpaPagingItemReader<GroupPurchaseWithProduct> openGroupPurchase(
            @Value("#{jobParameters['now']}") String now
    ) {

        OffsetDateTime parsedNow = OffsetDateTime.parse(now);
        return new JpaPagingItemReaderBuilder<GroupPurchaseWithProduct>()
                .name("openGroupPurchaseReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT new store._0982.batch.batch.grouppurchase.dto.GroupPurchaseWithProduct(g,p) " +
                        "FROM GroupPurchase g, Product p " +
                        "WHERE p.productId = g.productId " +
                        "AND g.status = :status " +
                        "AND g.startDate <= :now"
                )
                .parameterValues(Map.of(
                        "status", GroupPurchaseStatus.SCHEDULED,
                        "now", parsedNow
                ))
                .pageSize(20)
                .build();
    }
}
