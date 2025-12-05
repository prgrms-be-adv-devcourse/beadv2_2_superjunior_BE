package store._0982.product.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.product.application.dto.ProductRegisterCommand;
import store._0982.product.application.dto.ProductRegisterInfo;
import store._0982.product.common.exception.CustomErrorCode;
import store._0982.product.common.exception.CustomException;
import store._0982.product.domain.Product;
import store._0982.product.domain.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductRegisterInfo createProduct(ProductRegisterCommand command) {
        if (command.memberRole().equals("CONSUMER")) {
            throw new CustomException(CustomErrorCode.NON_SELLER_ACCESS_DENIED);
        }
        Product product = new Product(command.name(),
                command.price(), command.category(),
                command.description(), command.stock(),
                command.originalUrl(), command.sellerId());

        Product savedProduct = productRepository.save(product);
        return ProductRegisterInfo.from(savedProduct);
    }
}
