package store._0982.elasticsearch.infrastructure.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRepository;
import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRow;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class GroupPurchaseSearchRepositoryAdapter implements GroupPurchaseSearchRepository {

    private final GroupPurchaseSearchJpaRepository repository;

    @Override
    public List<GroupPurchaseSearchRow> findAllByIds(List<java.util.UUID> groupPurchaseIds) {
        return repository.findAllByIds(groupPurchaseIds)
                .stream()
                .map(this::toRow)
                .toList();
    }

    private GroupPurchaseSearchRow toRow(GroupPurchaseSearchProjection projection) {
        return new GroupPurchaseSearchRow(
                projection.getGroupPurchaseId(),
                projection.getMinQuantity(),
                projection.getMaxQuantity(),
                projection.getTitle(),
                projection.getDescription(),
                projection.getDiscountedPrice(),
                projection.getStatus(),
                projection.getStartDate(),
                projection.getEndDate(),
                projection.getCreatedAt(),
                projection.getUpdatedAt(),
                projection.getCurrentQuantity(),
                projection.getProductId(),
                projection.getCategory(),
                projection.getPrice(),
                projection.getOriginalUrl(),
                projection.getSellerId()
        );
    }
}
