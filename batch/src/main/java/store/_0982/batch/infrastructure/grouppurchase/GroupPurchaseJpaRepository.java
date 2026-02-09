package store._0982.batch.infrastructure.grouppurchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface GroupPurchaseJpaRepository extends JpaRepository<GroupPurchase, UUID> {

    Page<GroupPurchase> findAllBySellerId(UUID sellerId, Pageable pageable);

    @Modifying
    @Query("UPDATE GroupPurchase g SET g.status = 'OPEN' " +
            "WHERE g.status = 'SCHEDULED' " +
            "AND g.startDate <= :now")
    int openReadyGroupPurchases(@Param("now") OffsetDateTime now);

    boolean existsByProductId(UUID productId);

    List<GroupPurchase> findAllByStatusAndStartDateBefore(GroupPurchaseStatus status, OffsetDateTime now);
    
    List<GroupPurchase> findAllByGroupPurchaseIdIn(List<UUID> groupPurchaseIds);

    boolean existsByProductIdAndStatusIn(UUID productId, List<GroupPurchaseStatus> statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update GroupPurchase gp
            set gp.settledAt = :now
            where gp.groupPurchaseId in :uuids
            """
    )
    void markAsSettled(
            @Param("uuids") List<UUID> uuids,
            @Param("now") OffsetDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE GroupPurchase g SET g.status = :status, g.updatedAt = :now WHERE g.groupPurchaseId IN :ids")
    int bulkUpdateStatus(@Param("ids") List<UUID> ids,
                         @Param("status") GroupPurchaseStatus status,
                         @Param("now") OffsetDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE GroupPurchase  g SET g.status = :status, g.succeededAt = :now, g.updatedAt = :now WHERE g.groupPurchaseId IN :ids")
    int bulkUpdateStatusWithSucceededAt(@Param("ids") List<UUID> ids,
                         @Param("status") GroupPurchaseStatus status,
                         @Param("now") OffsetDateTime now);
}
