package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchasePerformanceInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.product.Product;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupPurchasePerformanceService {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductRepository productRepository;

    public List<GroupPurchasePerformanceInfo> getPerformance(List<UUID> groupPurchaseIds){
        List<GroupPurchase> purchases = groupPurchaseRepository.findAllByGroupPurchaseIdIn(groupPurchaseIds);

        List<UUID> productIds = purchases.stream()
                .map(GroupPurchase::getProductId)
                .distinct()
                .toList();

        Map<UUID, Product> productMap = productRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getProductId, p->p));

        return purchases.stream()
                .map(purchase -> toPerformanceInfo(purchase, productMap.get(purchase.getProductId())))
                .toList();

    }

    private GroupPurchasePerformanceInfo toPerformanceInfo(GroupPurchase purchase, Product product){
        Long originalPrice = product.getPrice();
        Long discountedPrice = purchase.getDiscountedPrice();

        double discountRate = 0.0;

        if(originalPrice != null && originalPrice > 0){
            discountRate = (1.0 - ((double) discountedPrice / (double) originalPrice)) * 100.0;
        }

        double participationRate = 0.0;
        if(purchase.getMaxQuantity()>0){
            participationRate = ((double) purchase.getCurrentQuantity() / (double) purchase.getMaxQuantity()) * 100.0;
        }

        int duration = (int) ChronoUnit.DAYS.between(
                purchase.getStartDate(),
                purchase.getEndDate()
        );

        return new GroupPurchasePerformanceInfo(
                purchase.getGroupPurchaseId(),
                purchase.getTitle(),
                product.getCategory(),
                originalPrice,
                discountedPrice,
                discountRate,
                purchase.getMinQuantity(),
                purchase.getMaxQuantity(),
                purchase.getCurrentQuantity(),
                purchase.getStatus(),
                participationRate,
                duration
        );
    }
}
