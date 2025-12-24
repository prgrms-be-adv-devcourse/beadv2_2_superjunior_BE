package store._0982.batch.domain.grouppurchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupPurchaseRepository {
    List<GroupPurchase> saveAll(List<GroupPurchase> groupPurchaseList);

    Optional<GroupPurchase> findById(UUID purchaseId);

    List<GroupPurchase> findAllByStatusAndStartDateBefore(GroupPurchaseStatus status, OffsetDateTime now);
}

