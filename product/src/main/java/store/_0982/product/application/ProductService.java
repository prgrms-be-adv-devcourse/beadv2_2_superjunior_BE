package store._0982.product.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.product.application.dto.ProductRegisterCommand;
import store._0982.product.application.dto.ProductRegisterInfo;
import store._0982.product.application.dto.ProductDetailInfo;
import store._0982.product.application.dto.ProductUpdateCommand;
import store._0982.product.application.dto.ProductUpdateInfo;
import store._0982.product.common.dto.ResponseDto;
import store._0982.product.common.exception.CustomErrorCode;
import store._0982.product.common.exception.CustomException;
import store._0982.product.domain.Product;
import store._0982.product.domain.ProductRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductRegisterInfo createProduct(ProductRegisterCommand command) {
        if (!command.memberRole().equals("SELLER") && !command.memberRole().equals("ADMIN")) {
            throw new CustomException(CustomErrorCode.NON_SELLER_ACCESS_DENIED);
        }
        Product product = new Product(command.name(),
                command.price(), command.category(),
                command.description(), command.stock(),
                command.originalUrl(), command.sellerId());

        Product savedProduct = productRepository.save(product);
        return ProductRegisterInfo.from(savedProduct);
    }

    public void deleteProduct(UUID productId, UUID memberId) {
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        if (!findProduct.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_NOT_PRODUCT_OWNER);
        }

        productRepository.delete(findProduct);
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

        return ProductUpdateInfo.from(product);
    }
}
