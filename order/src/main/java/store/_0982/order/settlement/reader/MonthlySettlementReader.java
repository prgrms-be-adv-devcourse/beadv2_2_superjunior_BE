package store._0982.order.settlement.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;
import store._0982.order.settlement.policy.SettlementPolicy;
import store._0982.order.domain.settlement.SellerBalance;

import java.util.Map;

/**
 * 월간 정산 대상 조회 Reader
 * - 최소 송금 금액(3만원) 이상인 판매자 잔액 조회
 */
@Component
@RequiredArgsConstructor
public class MonthlySettlementReader {

    private final EntityManagerFactory entityManagerFactory;

    public JpaPagingItemReader<SellerBalance> create() {
        return new JpaPagingItemReaderBuilder<SellerBalance>()
                .name("monthlySettlementReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(10)
                .queryString("""
                        SELECT s
                        FROM SellerBalance s
                        WHERE s.settlementBalance >= :amount
                        ORDER BY s.balanceId ASC
                        """)
                .parameterValues(Map.of(
                        "amount", SettlementPolicy.MINIMUM_TRANSFER_AMOUNT
                ))
                .build();
    }
}
