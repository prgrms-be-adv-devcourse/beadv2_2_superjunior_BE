package store._0982.batch.domain.grouppurchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupPurchaseRepository {
	GroupPurchase save(GroupPurchase groupPurchase);

    List<GroupPurchase> saveAll(List<GroupPurchase> groupPurchaseList);
}

