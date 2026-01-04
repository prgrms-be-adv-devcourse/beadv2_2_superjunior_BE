package store._0982.commerce.integration.sellerbalance.fixture;

import store._0982.commerce.domain.sellerbalance.SellerBalance;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

public class SellerBalanceFixture {

    public static SellerBalance create(UUID memberId) {
        try {
            Constructor<SellerBalance> constructor = SellerBalance.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            SellerBalance sellerBalance = constructor.newInstance();

            setField(sellerBalance, "balanceId", UUID.randomUUID());
            setField(sellerBalance, "memberId", memberId);
            setField(sellerBalance, "settlementBalance", 0L);

            return sellerBalance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SellerBalance fixture", e);
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
