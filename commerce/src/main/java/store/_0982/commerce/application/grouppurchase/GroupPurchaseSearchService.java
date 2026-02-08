package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseSearchRow;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.product.Product;

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
        List<UUID> productIds = groupPurchases.stream()
                .map(GroupPurchase::getProductId)
                .distinct()
                .toList();
        Map<UUID, Product> productMap = productRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        List<GroupPurchaseSearchRow> rows = new ArrayList<>(groupPurchases.size());
        for (GroupPurchase groupPurchase : groupPurchases) {
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
}
