package store._0982.batch.batch.sellerbalance.writer;

import java.util.UUID;

public record GroupPurchaseAmountRow(
        UUID groupPurchaseId,
        Long totalAmount
) {
}
