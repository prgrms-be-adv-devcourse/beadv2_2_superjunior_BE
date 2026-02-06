package store._0982.elasticsearch.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import store._0982.common.dto.PageResponse;
import store._0982.common.log.ServiceLog;
import store._0982.elasticsearch.application.dto.GroupPurchaseSearchInfo;
import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRepository;
import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRow;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupPurchaseSearchRdbService {

    private final GroupPurchaseSearchRepository repository;

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
        List<GroupPurchaseSearchRow> rows = repository.searchByCondition(
                safeKeyword,
                safeStatus,
                safeCategory,
                sellerId,
                pageable.getPageSize(),
                pageable.getOffset()
        );
        List<GroupPurchaseSearchInfo> mapped = rows.stream()
                .map(GroupPurchaseSearchInfo::from)
                .toList();
        return PageResponse.from(new PageImpl<>(mapped, pageable, total));
    }
}
