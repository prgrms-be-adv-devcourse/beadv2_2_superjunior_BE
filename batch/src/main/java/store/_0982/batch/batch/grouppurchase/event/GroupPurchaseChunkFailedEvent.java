package store._0982.batch.batch.grouppurchase.event;

import store._0982.batch.domain.grouppurchase.GroupPurchase;

import java.util.List;

public record GroupPurchaseChunkFailedEvent(
        List<GroupPurchase> failedGroupPurchases
) {
}
