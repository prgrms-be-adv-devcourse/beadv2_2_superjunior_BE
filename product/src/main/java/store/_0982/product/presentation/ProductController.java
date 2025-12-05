package store._0982.product.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.product.application.ProductService;
import store._0982.product.application.dto.ProductDetailInfo;
import store._0982.product.application.dto.ProductUpdateInfo;
import store._0982.product.common.dto.ResponseDto;
import store._0982.product.presentation.dto.ProductUpdateRequest;

import java.util.UUID;

@Tag(name="Product", description = "상품 정보 관련")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "상품 정보 조회", description = "선택한 상품의 정보를 조회한다.")
    @GetMapping("/{productId}")
    public ResponseDto<ProductDetailInfo> getProductInfo(
            @PathVariable UUID productId
    ){
        ProductDetailInfo response = productService.getProductInfo(productId);
        return new ResponseDto<>(HttpStatus.OK.value(), response, "상품 조회가 완료되었습니다.");
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @Operation(summary = "상품 정보 수정", description = "판매자 정보를 수정한다.")
    @PatchMapping("/{productId}")
    public ResponseDto<ProductUpdateInfo> updateProduct(
            @PathVariable UUID productId,
            @RequestBody ProductUpdateRequest request){
        ProductUpdateInfo response = productService.updateProduct(productId, request.toCommand());
        return new ResponseDto<>(HttpStatus.OK.value(), response, "상품 수정이 완료되었습니다.");
    }
}
