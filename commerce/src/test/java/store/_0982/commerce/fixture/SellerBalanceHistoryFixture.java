package store._0982.commerce.fixture;

import store._0982.commerce.domain.sellerbalance.SellerBalanceHistory;
import store._0982.commerce.domain.sellerbalance.SellerBalanceHistoryStatus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

public class SellerBalanceHistoryFixture {

    public static SellerBalanceHistory create(
            UUID memberId,
            UUID settlementId,
            Long amount,
            SellerBalanceHistoryStatus status
    ) {
        try {
            Constructor<SellerBalanceHistory> constructor = SellerBalanceHistory.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            SellerBalanceHistory history = constructor.newInstance();

            setField(history, "historyId", UUID.randomUUID());
            setField(history, "memberId", memberId);
            setField(history, "settlementId", settlementId);
            setField(history, "amount", amount);
            setField(history, "status", status);

            return history;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SellerBalanceHistory fixture", e);
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}