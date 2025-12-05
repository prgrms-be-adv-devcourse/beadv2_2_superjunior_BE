package store._0982.product.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * 상품 업데이트
     * @param productId 상품 id
     * @param command 업데이트할 command 데이터
     * @return ProductUpdateInfo
     */
    public ResponseDto<ProductUpdateInfo> updateProduct(UUID productId, ProductUpdateCommand command){
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        product.updateProduct(
                command.name(),
                command.price(),
                command.category(),
                command.description(),
                command.stock(),
                command.originalLink());
        return new ResponseDto<>(HttpStatus.OK.value(), ProductUpdateInfo.from(product), "상품 업데이트가 완료되었습니다.");
    }

    /**
     * 상품 정보 조회
     * @param productId 상품 id
     * @return ProductDetailInfo
     */
    public ResponseDto<ProductDetailInfo> getProductInfo(UUID  productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        return new ResponseDto<>(HttpStatus.OK.value(), ProductDetailInfo.from(product), "상품 조회가 완료되었습니다.");
    }
}
