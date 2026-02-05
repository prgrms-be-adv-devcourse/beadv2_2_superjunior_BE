package store._0982.elasticsearch.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import store._0982.common.dto.PageResponse;
import store._0982.common.log.ServiceLog;
import store._0982.elasticsearch.application.dto.GroupPurchaseSearchInfo;
import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRow;
import store._0982.elasticsearch.infrastructure.search.GroupPurchaseSearchJpaRepository;
import store._0982.elasticsearch.infrastructure.search.GroupPurchaseSearchProjection;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupPurchaseSearchRdbService {

    private final GroupPurchaseSearchJpaRepository repository;

    @ServiceLog
    public PageResponse<GroupPurchaseSearchInfo> searchByKeyword(
            String keyword,
            String status,
            String category,
            java.util.UUID sellerId,
            Pageable pageable
    ) {
        String safeKeyword = keyword == null ? "" : keyword;
        String safeStatus = status == null || status.isBlank() ? null : status;
        String safeCategory = category == null || category.isBlank() ? null : category;
        long total = repository.countByCondition(safeKeyword, safeStatus, safeCategory, sellerId);
        List<GroupPurchaseSearchProjection> rows = repository.searchByCondition(
                safeKeyword,
                safeStatus,
                safeCategory,
                sellerId,
                pageable.getPageSize(),
                pageable.getOffset()
        );
        List<GroupPurchaseSearchInfo> mapped = rows.stream()
                .map(GroupPurchaseSearchRdbService::toInfo)
                .toList();
        return PageResponse.from(new PageImpl<>(mapped, pageable, total));
    }

    private static GroupPurchaseSearchInfo toInfo(GroupPurchaseSearchProjection row) {
        GroupPurchaseSearchRow mapped = new GroupPurchaseSearchRow(
                row.getGroupPurchaseId(),
                row.getMinQuantity(),
                row.getMaxQuantity(),
                row.getTitle(),
                row.getDescription(),
                row.getImageUrl(),
                row.getDiscountedPrice(),
                row.getStatus(),
                row.getStartDate(),
                row.getEndDate(),
                row.getCreatedAt(),
                row.getUpdatedAt(),
                row.getCurrentQuantity(),
                row.getProductId(),
                row.getCategory(),
                row.getPrice(),
                row.getOriginalUrl(),
                row.getSellerId()
        );
        return GroupPurchaseSearchInfo.from(mapped);
    }
}
