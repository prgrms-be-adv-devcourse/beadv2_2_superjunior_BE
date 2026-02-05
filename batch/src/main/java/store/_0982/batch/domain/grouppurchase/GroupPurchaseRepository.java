package store._0982.batch.domain.grouppurchase;

import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;

import java.util.List;
import java.util.UUID;

public interface GroupPurchaseRepository {
	GroupPurchase save(GroupPurchase groupPurchase);

    List<GroupPurchase> saveAll(List<GroupPurchase> groupPurchaseList);

    int bulkUpdateStatus(List<UUID> ids, GroupPurchaseStatus status);

    int bulkUpdateStatusWithSucceededAt(List<UUID> ids, GroupPurchaseStatus status);
}

