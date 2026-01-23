package store._0982.batch.batch.grouppurchase.event;

import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultWithProductInfo;

import java.util.List;

public record GroupPurchaseChunkUpdateEvent(
    List<GroupPurchaseResultWithProductInfo> updatedItems
) {
}
