package store._0982.elasticsearch.infrastructure.search;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import store._0982.elasticsearch.domain.reindex.GroupPurchaseReadEntity;

import java.util.List;
import java.util.UUID;

public interface GroupPurchaseSearchJpaRepository extends Repository<GroupPurchaseReadEntity, UUID> {

    @Query(value = """
            select
                gp.group_purchase_id as groupPurchaseId,
                gp.min_quantity as minQuantity,
                gp.max_quantity as maxQuantity,
                gp.title as title,
                gp.description as description,
                gp.discounted_price as discountedPrice,
                gp.status as status,
                gp.start_date as startDate,
                gp.end_date as endDate,
                gp.created_at as createdAt,
                gp.updated_at as updatedAt,
                gp.current_quantity as currentQuantity,
                p.product_id as productId,
                p.category as category,
                p.price as price,
                p.original_url as originalUrl,
                p.seller_id as sellerId
            from product_schema.group_purchase gp
            join product_schema.product p on p.product_id = gp.product_id
            where gp.group_purchase_id in (:ids)
            """, nativeQuery = true)
    List<GroupPurchaseSearchProjection> findAllByIds(@Param("ids") List<UUID> ids);
}
