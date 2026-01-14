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
 * 공동구매 종료 대상 Reader
 */
@Component
@RequiredArgsConstructor
public class UpdateStatusClosedGroupPurchaseReader {

    private final EntityManagerFactory entityManagerFactory;

    public JpaPagingItemReader<GroupPurchase> create() {
        return new JpaPagingItemReaderBuilder<GroupPurchase>()
                .name("UpdateStatusClosedGroupPurchaseReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT g FROM GroupPurchase g " +
                        "WHERE g.status = :status " +
                        "AND g.endDate <= :now"
                )
                .parameterValues(Map.of(
                        "status", GroupPurchaseStatus.OPEN,
                        "now", OffsetDateTime.now()
                ))
                .pageSize(20)
                .build();
    }
}
