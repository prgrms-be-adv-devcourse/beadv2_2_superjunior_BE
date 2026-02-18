package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseSearchInfo;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseSearchRow;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.product.ProductCategory;
import store._0982.common.dto.PageResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupPurchaseSearchService {

    private final GroupPurchaseRepository groupPurchaseRepository;

    public List<GroupPurchaseSearchRow> findSearchRowsByIds(List<UUID> purchaseIds) {
        if (purchaseIds == null || purchaseIds.isEmpty()) {
            return List.of();
        }
        List<GroupPurchaseSearchRow> rows = groupPurchaseRepository.findSearchRowsByIds(purchaseIds);
        if (rows.isEmpty()) {
            return List.of();
        }
        var rowMap = rows.stream()
                .collect(java.util.stream.Collectors.toMap(GroupPurchaseSearchRow::groupPurchaseId, java.util.function.Function.identity()));

        List<GroupPurchaseSearchRow> ordered = new ArrayList<>(purchaseIds.size());
        for (UUID purchaseId : purchaseIds) {
            GroupPurchaseSearchRow row = rowMap.get(purchaseId);
            if (row != null) {
                ordered.add(row);
            }
        }
        return ordered;
    }

    public PageResponse<GroupPurchaseSearchInfo> searchGroupPurchasesByDb(
            String keyword,
            String status,
            UUID sellerId,
            String category,
            Pageable pageable
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        GroupPurchaseStatus statusFilter;
        ProductCategory categoryFilter;
        try {
            statusFilter = parseStatus(status);
            categoryFilter = parseCategory(category);
        } catch (IllegalArgumentException ex) {
            return PageResponse.from(new PageImpl<>(List.of(), pageable, 0));
        }

        Page<GroupPurchaseSearchRow> page = groupPurchaseRepository.searchRows(
                normalizedKeyword,
                statusFilter,
                categoryFilter,
                sellerId,
                pageable
        );
        Page<GroupPurchaseSearchInfo> mapped = page.map(GroupPurchaseSearchInfo::from);
        return PageResponse.from(mapped);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private GroupPurchaseStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return GroupPurchaseStatus.valueOf(status.trim().toUpperCase());
    }

    private ProductCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        return ProductCategory.valueOf(category.trim().toUpperCase());
    }
}
