package store._0982.batch.batch.settlement.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;
import store._0982.batch.domain.sellerbalance.SellerBalance;

import java.util.Map;

/**
 * 저액 정산 대상 조회 Reader
 * - 0원 초과 & 최소 송금 금액(3만원) 미만인 판매자 잔액 조회
 * - 알림 대상
 */
@Component
@RequiredArgsConstructor
public class lowBalanceNotificationReader {

    private final EntityManagerFactory entityManagerFactory;

    public JpaPagingItemReader<SellerBalance> create() {
        return new JpaPagingItemReaderBuilder<SellerBalance>()
                .name("lowBalanceReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(SettlementPolicy.CHUNK_UNIT)
                .queryString("""
                        SELECT s
                        FROM SellerBalance s
                        WHERE s.settlementBalance > 0
                          AND s.settlementBalance < :amount
                        ORDER BY s.balanceId ASC
                        """)
                .parameterValues(Map.of(
                        "amount", SettlementPolicy.MINIMUM_TRANSFER_AMOUNT
                ))
                .build();
    }
}
