package store._0982.elasticsearch.infrastructure.reindex;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface GroupPurchaseReindexJpaRepository extends Repository<GroupPurchaseReadEntity, UUID> {

    @Query(value = """
            select
                gp.group_purchase_id as groupPurchaseId,
                gp.title as title,
                gp.description as description,
                gp.status as status,
                gp.start_date as startDate,
                gp.end_date as endDate,
                gp.min_quantity as minQuantity,
                gp.max_quantity as maxQuantity,
                gp.discounted_price as discountedPrice,
                gp.current_quantity as currentQuantity,
                gp.created_at as createdAt,
                gp.updated_at as updatedAt,
                p.product_id as productId,
                p.category as category,
                p.price as price,
                p.original_url as originalUrl,
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
                gp.start_date as startDate,
                gp.end_date as endDate,
                gp.min_quantity as minQuantity,
                gp.max_quantity as maxQuantity,
                gp.discounted_price as discountedPrice,
                gp.current_quantity as currentQuantity,
                gp.created_at as createdAt,
                gp.updated_at as updatedAt,
                p.product_id as productId,
                p.category as category,
                p.price as price,
                p.original_url as originalUrl,
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
            select count(*)
            from product_schema.group_purchase gp
            join product_schema.product p on p.product_id = gp.product_id
            """, nativeQuery = true)
    long countSource();
}
