package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseSearchInfo;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseSearchRow;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.product.ProductCategory;
import store._0982.common.domain.product.Product;
import store._0982.common.dto.PageResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupPurchaseSearchService {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductRepository productRepository;

    public List<GroupPurchaseSearchRow> findSearchRowsByIds(List<UUID> purchaseIds) {
        if (purchaseIds == null || purchaseIds.isEmpty()) {
            return List.of();
        }
        List<GroupPurchase> groupPurchases = groupPurchaseRepository.findAllByGroupPurchaseIdIn(purchaseIds);
        if (groupPurchases.isEmpty()) {
            return List.of();
        }
        Map<UUID, GroupPurchase> groupPurchaseMap = groupPurchases.stream()
                .collect(Collectors.toMap(GroupPurchase::getGroupPurchaseId, Function.identity()));
        List<UUID> productIds = groupPurchases.stream()
                .map(GroupPurchase::getProductId)
                .distinct()
                .toList();
        Map<UUID, Product> productMap = productRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        List<GroupPurchaseSearchRow> rows = new ArrayList<>(purchaseIds.size());
        for (UUID purchaseId : purchaseIds) {
            GroupPurchase groupPurchase = groupPurchaseMap.get(purchaseId);
            if (groupPurchase == null) {
                continue;
            }
            Product product = productMap.get(groupPurchase.getProductId());
            if (product == null) {
                continue;
            }
            rows.add(new GroupPurchaseSearchRow(
                    groupPurchase.getGroupPurchaseId(),
                    groupPurchase.getMinQuantity(),
                    groupPurchase.getMaxQuantity(),
                    groupPurchase.getTitle(),
                    groupPurchase.getDescription(),
                    groupPurchase.getImageUrl(),
                    groupPurchase.getDiscountedPrice(),
                    groupPurchase.getStatus().name(),
                    groupPurchase.getStartDate().toInstant(),
                    groupPurchase.getEndDate().toInstant(),
                    groupPurchase.getCreatedAt().toInstant(),
                    groupPurchase.getUpdatedAt() == null ? null : groupPurchase.getUpdatedAt().toInstant(),
                    groupPurchase.getCurrentQuantity(),
                    product.getProductId(),
                    product.getCategory().name(),
                    product.getPrice(),
                    product.getOriginalUrl(),
                    groupPurchase.getSellerId()
            ));
        }
        return rows;
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
