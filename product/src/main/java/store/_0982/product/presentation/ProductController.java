package store._0982.product.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.product.application.ProductService;
import store._0982.product.application.dto.ProductRegisterInfo;
import store._0982.product.common.dto.ResponseDto;
import store._0982.product.presentation.dto.ProductRegisterRequest;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<ProductRegisterInfo> createProduct(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestHeader("X-Member-Role") String memberRole,
            @RequestBody ProductRegisterRequest request) {
        ProductRegisterInfo info = productService.createProduct(request.toCommand(memberId, memberRole));
        return new ResponseDto<>(HttpStatus.CREATED, info, "상품이 등록되었습니다.");
    }

}
