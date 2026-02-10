package store._0982.commerce.presentation.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store._0982.commerce.application.product.ProductService;
import store._0982.commerce.application.product.dto.ProductInfoForVector;
import store._0982.common.dto.PageResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("internal/products")
class InternalProductController {

    private final ProductService productService;

    @GetMapping("vector")
    public PageResponse<ProductInfoForVector> getProducts(@RequestParam("page") int page, @RequestParam("size") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.from(productService.findAllProductsForVector(pageable));
    }
}
