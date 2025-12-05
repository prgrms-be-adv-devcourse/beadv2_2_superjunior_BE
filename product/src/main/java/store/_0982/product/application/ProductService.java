package store._0982.product.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.product.application.dto.ProductRegisterCommand;
import store._0982.product.application.dto.ProductRegisterInfo;
import store._0982.product.common.exception.CustomErrorCode;
import store._0982.product.common.exception.CustomException;
import store._0982.product.domain.Product;
import store._0982.product.domain.ProductRepository;

import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
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
}
