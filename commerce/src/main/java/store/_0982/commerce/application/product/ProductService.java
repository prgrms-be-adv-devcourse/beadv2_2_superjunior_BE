package store._0982.commerce.application.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.product.dto.*;
import store._0982.commerce.application.product.event.ProductCreatedEvent;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @ServiceLog
    @Transactional
    public ProductRegisterInfo createProduct(ProductRegisterCommand command) {
        Optional<Product> existing = productRepository
                .findByIdempotencyKey(command.idempotencyKey());

        if (existing.isPresent()) {
            return ProductRegisterInfo.from(existing.get());
        }

        Product product = Product.createProduct(command.name(),
                command.price(), command.category(),
                command.description(), command.stock(),
                command.originalUrl(), command.idempotencyKey(),
                command.sellerId());

        Product savedProduct = productRepository.save(product);

        // AI 모듈 kafka
        eventPublisher.publishEvent(new ProductCreatedEvent(product));

        return ProductRegisterInfo.from(savedProduct);
    }

    /**
     * 상품 정보 조회
     * @param productId 상품 id
     * @return ProductDetailInfo
     */
    public ProductDetailInfo getProductInfo(UUID  productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        return ProductDetailInfo.from(product);
    }

    public PageResponse<ProductListInfo> getProductListInfo(UUID sellerId, Pageable pageable) {
        Page<Product> product = productRepository.findBySellerId(sellerId, pageable);

        Page<ProductListInfo> products = product.map(ProductListInfo::from);

        return PageResponse.from(products);
    }

    /**
     * 상품 업데이트
     * @param productId 상품 id
     * @param command 업데이트할 command 데이터
     * @return ProductUpdateInfo
     */
    @Transactional
    public ProductUpdateInfo updateProduct(UUID productId, ProductUpdateCommand command){
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        product.updateProduct(
                command.name(),
                command.price(),
                command.category(),
                command.description(),
                command.stock(),
                command.originalLink());

        return ProductUpdateInfo.from(product);
    }

    @Transactional
    public void deleteProduct(UUID productId, UUID memberId) {
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        if (!findProduct.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_NOT_PRODUCT_OWNER);
        }

        List<GroupPurchaseStatus> groupPurchaseStatuses = List.of(GroupPurchaseStatus.SCHEDULED, GroupPurchaseStatus.OPEN);
        if (groupPurchaseRepository.existsByProductIdAndStatusIn(productId, groupPurchaseStatuses)) {
            throw new CustomException(CustomErrorCode.PRODUCT_ACTIVE_GROUP_PURCHASE_EXISTS);
        }

        boolean isUsedInGroupPurchase = groupPurchaseRepository.existsByProductId(productId);

        if (isUsedInGroupPurchase) {
            // soft delete: 상태를 삭제로 변경
            findProduct.softDelete();
            productRepository.save(findProduct);
        } else {
            // hard delete
            productRepository.delete(findProduct);
        }
    }

}
