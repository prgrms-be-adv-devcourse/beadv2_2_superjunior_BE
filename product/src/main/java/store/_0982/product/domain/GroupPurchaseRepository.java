package store._0982.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupPurchaseRepository {
	GroupPurchase save(GroupPurchase groupPurchase);

    Optional<GroupPurchase> findById(UUID purchaseId);

    Page<GroupPurchase> findAll(Pageable pageable);

    Page<GroupPurchase> findAllBySellerId(UUID sellerId, Pageable pageable);

    void delete(GroupPurchase groupPurchase);

    GroupPurchase saveAndFlush(GroupPurchase groupPurchase);
  
    List<GroupPurchase> findByStatusAndSettledAtIsNull(GroupPurchaseStatus status);

    List<GroupPurchase> saveAll(List<GroupPurchase> groupPurchaseList);

    int openReadyGroupPurchases(OffsetDateTime now);

    List<GroupPurchase> findAllByGroupPurchaseIdIn(List<UUID> groupPurchaseIds);

}
