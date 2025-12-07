package store._0982.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GroupPurchaseRepository {

    Optional<GroupPurchase> findById(UUID purchaseId);

    Page<GroupPurchase> findAll(Pageable pageable);

    Page<GroupPurchase> findAllBySellerId(UUID sellerId, Pageable pageable);

    void delete(GroupPurchase groupPurchase);

}
