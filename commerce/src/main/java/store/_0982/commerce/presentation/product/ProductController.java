package store._0982.commerce.presentation.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.commerce.application.product.ProductService;
import store._0982.commerce.application.product.dto.ProductListInfo;
import store._0982.commerce.application.product.dto.ProductRegisterInfo;
import store._0982.commerce.application.product.dto.ProductDetailInfo;
import store._0982.commerce.application.product.dto.ProductUpdateInfo;
import store._0982.commerce.presentation.product.dto.ProductRegisterRequest;
import store._0982.commerce.presentation.product.dto.ProductUpdateRequest;

import java.util.UUID;

@Tag(name="Product", description = "상품 정보 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @ControllerLog
    @Operation(summary = "상품 등록", description = "판매자가 새로운 상품을 등록한다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<ProductRegisterInfo> createProduct(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @Valid @RequestBody ProductRegisterRequest request) {
        ProductRegisterInfo info = productService.createProduct(request.toCommand(memberId));
        return new ResponseDto<>(HttpStatus.CREATED, info, "상품이 등록되었습니다.");
    }

    @ControllerLog
    @Operation(summary = "상품 삭제", description = "판매자가 자신이 등록한 상품을 삭제한다.")
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<ProductRegisterInfo> deleteProduct(
            @PathVariable UUID productId,
            @RequestHeader(HeaderName.ID) UUID memberId) {
        productService.deleteProduct(productId, memberId);
        return new ResponseDto<>(HttpStatus.OK, null, "상품이 삭제되었습니다.");
    }

    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "상품 정보 조회", description = "선택한 상품의 정보를 조회한다.")
    @GetMapping("/{productId}")
    public ResponseDto<ProductDetailInfo> getProductInfo(
            @PathVariable UUID productId
    ){
        ProductDetailInfo response = productService.getProductInfo(productId);
        return new ResponseDto<>(HttpStatus.OK, response, "상품 조회가 완료되었습니다.");
    }

    @Operation(summary = "내가 등록한 상품 조회(판매자)", description = "내가 등록한 상품 목록을 조회 합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponse<ProductListInfo>> getProductListInfo(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable ){
        PageResponse<ProductListInfo> response = productService.getProductListInfo(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, response, "내가 등록한 상품 목록 조회가 완료되었습니다.");
    }


    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "상품 정보 수정", description = "판매자 정보를 수정한다.")
    @PatchMapping("/{productId}")
    public ResponseDto<ProductUpdateInfo> updateProduct(
            @PathVariable UUID productId,
            @RequestBody ProductUpdateRequest request){
        ProductUpdateInfo response = productService.updateProduct(productId, request.toCommand());
        return new ResponseDto<>(HttpStatus.OK, response, "상품 수정이 완료되었습니다.");
    }

}
