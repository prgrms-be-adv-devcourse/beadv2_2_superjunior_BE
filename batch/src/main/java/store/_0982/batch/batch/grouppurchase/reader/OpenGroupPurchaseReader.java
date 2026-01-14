package store._0982.batch.batch.grouppurchase.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 공동구매 시작 대상 Reader
 */
@Component
@RequiredArgsConstructor
public class OpenGroupPurchaseReader {

    private final EntityManagerFactory entityManagerFactory;

    public JpaPagingItemReader<GroupPurchase> create() {
        return new JpaPagingItemReaderBuilder<GroupPurchase>()
                .name("groupPurchaseReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT g FROM GroupPurchase g " +
                        "WHERE g.status = :status " +
                        "AND g.startDate <= :now"
                )
                .parameterValues(Map.of(
                        "status", GroupPurchaseStatus.SCHEDULED,
                        "now", OffsetDateTime.now()
                ))
                .pageSize(20)
                .build();
    }
}
