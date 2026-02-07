package store._0982.batch.batch.grouppurchase.event;

import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;

import java.util.List;

public record GroupPurchaseChunkUpdateEvent(
        List<GroupPurchaseResultProjection> updatedItems
) {
}
