package store._0982.product.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.product.application.ProductService;
import store._0982.product.application.dto.ProductRegisterInfo;
import store._0982.product.application.dto.ProductDetailInfo;
import store._0982.product.application.dto.ProductUpdateInfo;
import store._0982.product.presentation.dto.ProductRegisterRequest;
import store._0982.product.presentation.dto.ProductUpdateRequest;
import store._0982.product.common.dto.ResponseDto;

import java.util.UUID;

@Tag(name="Product", description = "상품 정보 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록", description = "판매자가 새로운 상품을 등록한다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<ProductRegisterInfo> createProduct(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestHeader("X-Member-Role") String memberRole,
            @RequestBody ProductRegisterRequest request) {
        ProductRegisterInfo info = productService.createProduct(request.toCommand(memberId, memberRole));
        return new ResponseDto<>(HttpStatus.CREATED, info, "상품이 등록되었습니다.");
    }

    @Operation(summary = "상품 삭제", description = "판매자가 자신이 등록한 상품을 삭제한다.")
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseDto<ProductRegisterInfo> deleteProduct(
            @PathVariable UUID productId,
            @RequestHeader("X-Member-Id") UUID memberId) {
        productService.deleteProduct(productId, memberId);
        return new ResponseDto<>(HttpStatus.NO_CONTENT, null, "상품이 삭제되었습니다.");
    }

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
