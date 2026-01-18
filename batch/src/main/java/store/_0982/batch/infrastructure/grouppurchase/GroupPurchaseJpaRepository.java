package store._0982.batch.infrastructure.grouppurchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface GroupPurchaseJpaRepository extends JpaRepository<GroupPurchase, UUID> {

    Page<GroupPurchase> findAllBySellerId(UUID sellerId, Pageable pageable);

    List<GroupPurchase> findByStatusAndSettledAtIsNull(GroupPurchaseStatus status);

    @Modifying
    @Query("UPDATE GroupPurchase g SET g.status = 'OPEN' " +
            "WHERE g.status = 'SCHEDULED' " +
            "AND g.startDate <= :now")
    int openReadyGroupPurchases(@Param("now") OffsetDateTime now);

    boolean existsByProductId(UUID productId);

    List<GroupPurchase> findAllByStatusAndStartDateBefore(GroupPurchaseStatus status, OffsetDateTime now);
    
    List<GroupPurchase> findAllByGroupPurchaseIdIn(List<UUID> groupPurchaseIds);

    boolean existsByProductIdAndStatusIn(UUID productId, List<GroupPurchaseStatus> statuses);
}
