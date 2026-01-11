package store._0982.commerce.application.grouppurchase.event;

import java.util.UUID;

public record GroupPurchaseCountSyncEvent(
        UUID groupPurchaseId,
        int newCount
) { }
