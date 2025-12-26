package store._0982.commerce.application.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import store._0982.commerce.application.product.dto.ProductRegisterCommand;
import store._0982.commerce.application.product.dto.ProductRegisterInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.common.kafka.dto.ProductEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private GroupPurchaseRepository groupPurchaseRepository;

    @Mock
    private KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("상품 생성 Service")
    class CreateProductTest {

        @Test
        @DisplayName("상품을 생성한다")
        void createProduct_success() {
            // given
            UUID sellerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            OffsetDateTime now = OffsetDateTime.now();

            ProductRegisterCommand command = new ProductRegisterCommand(
                    "테스트 상품",
                    10000L,
                    ProductCategory.FOOD,
                    "맛있는 테스트 상품입니다.",
                    100,
                    "https://example.com/image.jpg",
                    sellerId
            );

            Product savedProduct = mock(Product.class);
            when(savedProduct.getProductId()).thenReturn(productId);
            when(savedProduct.getName()).thenReturn("테스트 상품");
            when(savedProduct.getPrice()).thenReturn(10000L);
            when(savedProduct.getCategory()).thenReturn(ProductCategory.FOOD);
            when(savedProduct.getDescription()).thenReturn("맛있는 테스트 상품입니다.");
            when(savedProduct.getStock()).thenReturn(100);
            when(savedProduct.getOriginalUrl()).thenReturn("https://example.com/image.jpg");
            when(savedProduct.getSellerId()).thenReturn(sellerId);
            when(savedProduct.getCreatedAt()).thenReturn(now);
            when(savedProduct.toEvent()).thenReturn(new ProductEvent(
                    productId,
                    "테스트 상품",
                    10000L,
                    "FOOD",
                    "맛있는 테스트 상품입니다.",
                    100,
                    "https://example.com/image.jpg",
                    sellerId,
                    now.toString(),
                    null
            ));

            when(productRepository.saveAndFlush(any(Product.class)))
                    .thenReturn(savedProduct);

            // when
            ProductRegisterInfo result = productService.createProduct(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.productId()).isEqualTo(productId);
            assertThat(result.name()).isEqualTo("테스트 상품");
            assertThat(result.price()).isEqualTo(10000L);
            assertThat(result.category()).isEqualTo(ProductCategory.FOOD);
            assertThat(result.description()).isEqualTo("맛있는 테스트 상품입니다.");
            assertThat(result.stock()).isEqualTo(100);
            assertThat(result.originalUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(result.sellerId()).isEqualTo(sellerId);
            assertThat(result.createdAt()).isEqualTo(now);

            verify(productRepository).saveAndFlush(any(Product.class));
            verify(kafkaTemplate).send(any(), any(), any());
        }
    }
}
