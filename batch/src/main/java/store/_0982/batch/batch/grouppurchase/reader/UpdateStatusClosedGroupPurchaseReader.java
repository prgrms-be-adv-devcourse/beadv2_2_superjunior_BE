package store._0982.batch.batch.grouppurchase.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 공동구매 종료 대상 Reader
 */
@Configuration
@RequiredArgsConstructor
public class UpdateStatusClosedGroupPurchaseReader {

    private final EntityManagerFactory entityManagerFactory;


    @Bean
    @StepScope
    public JpaPagingItemReader<GroupPurchase> updateStatusClosedGroupPurchase(
            @Value("#{jobParameters['now']}") String now
    ) {
        OffsetDateTime parsedNow = OffsetDateTime.parse(now);
        return new JpaPagingItemReaderBuilder<GroupPurchase>()
                .name("updateStatusClosedGroupPurchaseReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT g FROM GroupPurchase g " +
                        "WHERE g.status = :status " +
                        "AND g.endDate <= :now"
                )
                .parameterValues(Map.of(
                        "status", GroupPurchaseStatus.OPEN,
                        "now", parsedNow
                ))
                .pageSize(20)
                .build();
    }
}
