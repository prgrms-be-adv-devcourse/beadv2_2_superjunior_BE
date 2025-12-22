package store._0982.commerce.application.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.product.dto.*;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEvent;
import store._0982.common.log.ServiceLog;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductRepository;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @ServiceLog
    public ProductRegisterInfo createProduct(ProductRegisterCommand command) {
        Product product = new Product(command.name(),
                command.price(), command.category(),
                command.description(), command.stock(),
                command.originalUrl(), command.sellerId());

        Product savedProduct = productRepository.saveAndFlush(product);

        //kafka
        ProductEvent event = savedProduct.toEvent();
        kafkaTemplate.send(KafkaTopics.PRODUCT_UPSERTED,event.getId().toString(), event);

        return ProductRegisterInfo.from(savedProduct);
    }

    public void deleteProduct(UUID productId, UUID memberId) {
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        if (!findProduct.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_NOT_PRODUCT_OWNER);
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
        //kafka
        kafkaTemplate.send(KafkaTopics.PRODUCT_DELETED, findProduct.getProductId().toString(), findProduct.toEvent());
    }

    /**
     * 상품 정보 조회
     * @param productId 상품 id
     * @return ProductDetailInfo
     */
    @Transactional(readOnly = true)
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

        //kafka
        Product updatedProduct = productRepository.saveAndFlush(product);
        ProductEvent event = updatedProduct.toEvent();
        kafkaTemplate.send(KafkaTopics.PRODUCT_UPSERTED, event.getId().toString(), event);

        return ProductUpdateInfo.from(product);
    }
}
