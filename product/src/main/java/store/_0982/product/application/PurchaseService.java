package store._0982.product.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import store._0982.product.application.dto.GroupPurchaseInfo;
import store._0982.product.application.dto.GroupPurchaseThumbnailInfo;
import store._0982.product.common.dto.PageResponseDto;
import store._0982.product.common.exception.CustomErrorCode;
import store._0982.product.common.exception.CustomException;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseRepository;
import store._0982.product.domain.Product;
import store._0982.product.domain.ProductRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductRepository productRepository;

    public GroupPurchaseInfo getGroupPurchaseById(UUID purchaseId) {
        GroupPurchase findGroupPurchase = groupPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        Product findProduct = productRepository.findById(findGroupPurchase.getProductId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        // TODO : 참가자 수 집계 기능
        int participantCount = 0;
        return GroupPurchaseInfo.from(findGroupPurchase, participantCount, findProduct.getOriginalUrl(), findProduct.getPrice());
    }

    public PageResponseDto<GroupPurchaseThumbnailInfo> getGroupPurchase(Pageable pageable) {
        Page<GroupPurchase> groupPurchasePage = groupPurchaseRepository.findAll(pageable);

        Page<GroupPurchaseThumbnailInfo> groupPurchaseInfoPage = groupPurchasePage.map(
                groupPurchase -> {
                    // TODO : 참가자 수 집계 기능
                    int participantCount = 0;
                    return GroupPurchaseThumbnailInfo.from(groupPurchase, participantCount);
                });

        return PageResponseDto.from(groupPurchaseInfoPage);
    }

    public PageResponseDto<GroupPurchaseThumbnailInfo> getGroupPurchasesBySeller(UUID sellerId, Pageable pageable) {
        Page<GroupPurchase> groupPurchasePage = groupPurchaseRepository.findAllBySellerId(sellerId, pageable);

        Page<GroupPurchaseThumbnailInfo> groupPurchaseInfoPage = groupPurchasePage.map(
                groupPurchase -> {
                    // TODO : 참가자 수 집계 기능
                    int participantCount = 0;
                    return GroupPurchaseThumbnailInfo.from(groupPurchase, participantCount);
                });

        return PageResponseDto.from(groupPurchaseInfoPage);
    }

    public void deleteGroupPurchase(UUID purchaseId, UUID memberId) {
        GroupPurchase findGroupPurchase = groupPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        if (!findGroupPurchase.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_NOT_GROUP_PURCHASE_OWNER);
        }

        groupPurchaseRepository.delete(findGroupPurchase);
    }

}
