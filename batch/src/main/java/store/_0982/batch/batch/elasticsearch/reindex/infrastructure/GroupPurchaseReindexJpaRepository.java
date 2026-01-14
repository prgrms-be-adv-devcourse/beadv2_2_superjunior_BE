package store._0982.batch.batch.elasticsearch.reindex.infrastructure;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import store._0982.batch.domain.grouppurchase.GroupPurchase;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface GroupPurchaseReindexJpaRepository extends Repository<GroupPurchase, UUID> {

    @Query(value = """
            select
                gp.group_purchase_id as groupPurchaseId,
                gp.title as title,
                gp.description as description,
                gp.status as status,
                gp.end_date as endDate,
                gp.discounted_price as discountedPrice,
                gp.current_quantity as currentQuantity,
                coalesce(gp.updated_at, gp.created_at) as updatedAt,
                p.category as category,
                p.price as price,
                p.seller_id as sellerId
            from product_schema.group_purchase gp
            join product_schema.product p on p.product_id = gp.product_id
            order by gp.group_purchase_id
            limit :limit offset :offset
            """, nativeQuery = true)
    List<GroupPurchaseReindexProjection> findAllRows(
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    @Query(value = """
            select
                gp.group_purchase_id as groupPurchaseId,
                gp.title as title,
                gp.description as description,
                gp.status as status,
                gp.end_date as endDate,
                gp.discounted_price as discountedPrice,
                gp.current_quantity as currentQuantity,
                coalesce(gp.updated_at, gp.created_at) as updatedAt,
                p.category as category,
                p.price as price,
                p.seller_id as sellerId
            from product_schema.group_purchase gp
            join product_schema.product p on p.product_id = gp.product_id
            where coalesce(gp.updated_at, gp.created_at) >= :since
            order by gp.group_purchase_id
            limit :limit offset :offset
            """, nativeQuery = true)
    List<GroupPurchaseReindexProjection> findIncrementalRows(
            @Param("since") OffsetDateTime since,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    @Query(value = """
            select
                gp.group_purchase_id as groupPurchaseId,
                gp.title as title,
                gp.description as description,
                gp.status as status,
                gp.end_date as endDate,
                gp.discounted_price as discountedPrice,
                gp.current_quantity as currentQuantity,
                coalesce(gp.updated_at, gp.created_at) as updatedAt,
                p.category as category,
                p.price as price,
                p.seller_id as sellerId
            from product_schema.group_purchase gp
            join product_schema.product p on p.product_id = gp.product_id
            where gp.group_purchase_id in (:ids)
            """, nativeQuery = true)
    List<GroupPurchaseReindexProjection> findRowsByIds(@Param("ids") List<UUID> ids);

    @Query(value = """
            select count(*)
            from product_schema.group_purchase gp
            join product_schema.product p on p.product_id = gp.product_id
            """, nativeQuery = true)
    long countSource();
}
