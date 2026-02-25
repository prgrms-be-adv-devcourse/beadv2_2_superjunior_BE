package store._0982.commerce.infrastructure.grouppurchase;

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

    @Query("""
        SELECT g, p
        FROM GroupPurchase g
        JOIN Product p ON g.productId = p.productId
        WHERE g.groupPurchaseId IN :groupPurchaseIds
    """)
    List<Object[]> findSearchRowsByIdsWithProduct(
            @Param("groupPurchaseIds") List<UUID> groupPurchaseIds
    );

    @Modifying
    @Query("""
        UPDATE GroupPurchase g
        SET g.currentQuantity = g.currentQuantity + :quantity,
            g.status = CASE 
                WHEN g.currentQuantity + :quantity = g.maxQuantity THEN 'SUCCESS'
                ELSE g.status
            END 
        WHERE g.groupPurchaseId = :id
        AND g.status = 'OPEN'
        AND g.currentQuantity + :quantity <= g.maxQuantity
    """)
    int increaseQuantity(@Param("id") UUID id, @Param("quantity") int quantity);

    @Modifying
    @Query("""
        UPDATE GroupPurchase g
        SET g.currentQuantity = g.currentQuantity - :quantity
        WHERE g.groupPurchaseId = :id
        AND g.currentQuantity >= :quantity
    """)
    int decreaseQuantity(@Param("id") UUID id, @Param("quantity") int quantity);

    @Modifying
    @Query("""
        UPDATE GroupPurchase g
        SET g.currentQuantity = :currentQuantity,
            g.status = 'SUCCESS'
        WHERE g.groupPurchaseId = :id
        AND g.status = 'OPEN'
    """)
    int updateStatusToSuccess(@Param("id") UUID id, @Param("currentQuantity") int currentQuantity);

    @Modifying
    @Query("""
        UPDATE GroupPurchase g
        SET g.likeCount = g.likeCount + 1
        WHERE g.groupPurchaseId = :id
    """)
    void increaseLikeCount(@Param("id") UUID id);

    @Modifying
    @Query("""
        UPDATE GroupPurchase g
        SET g.likeCount = g.likeCount - 1
        WHERE g.groupPurchaseId = :id
    """)
    void decreaseLikeCount(@Param("id") UUID id);

}
