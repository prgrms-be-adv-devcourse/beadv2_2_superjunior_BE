package store._0982.commerce.presentation.product;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store._0982.commerce.application.product.ProductService;
import store._0982.commerce.application.product.dto.ProductDetailInfo;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class ProductInternalController {

    private final ProductService productService;

    @GetMapping("/products/{productId}")
    public ProductDetailInfo getProduct(
            @PathVariable UUID productId
    ){
        return productService.getProductInfo(productId);
    }
}
