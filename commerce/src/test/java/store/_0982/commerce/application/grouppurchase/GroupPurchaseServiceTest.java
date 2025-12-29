package store._0982.commerce.application.grouppurchase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseDetailInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.common.exception.CustomException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupPurchaseServiceTest {

    @Mock
    private GroupPurchaseRepository groupPurchaseRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GroupPurchaseService groupPurchaseService;

    @Nested
    @DisplayName("공동구매 상세 조회 Service")
    class GetGroupPurchaseByIdTest {

        @Test
        @DisplayName("공동구매를 상세 조회한다")
        void getGroupPurchaseById_success() {
            // given
            UUID purchaseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            UUID sellerId = UUID.randomUUID();
            OffsetDateTime now = OffsetDateTime.now();

            GroupPurchase groupPurchase = mock(GroupPurchase.class);
            when(groupPurchase.getGroupPurchaseId()).thenReturn(purchaseId);
            when(groupPurchase.getMinQuantity()).thenReturn(10);
            when(groupPurchase.getMaxQuantity()).thenReturn(100);
            when(groupPurchase.getTitle()).thenReturn("테스트 공동구매");
            when(groupPurchase.getDescription()).thenReturn("테스트 공동구매 설명입니다.");
            when(groupPurchase.getDiscountedPrice()).thenReturn(15000L);
            when(groupPurchase.getCurrentQuantity()).thenReturn(50);
            when(groupPurchase.getStartDate()).thenReturn(now.minusDays(1));
            when(groupPurchase.getEndDate()).thenReturn(now.plusDays(7));
            when(groupPurchase.getSellerId()).thenReturn(sellerId);
            when(groupPurchase.getProductId()).thenReturn(productId);
            when(groupPurchase.getStatus()).thenReturn(GroupPurchaseStatus.OPEN);
            when(groupPurchase.getCreatedAt()).thenReturn(now.minusDays(2));

            Product product = mock(Product.class);
            when(product.getPrice()).thenReturn(20000L);
            when(product.getCategory()).thenReturn(ProductCategory.FOOD);
            when(product.getOriginalUrl()).thenReturn("https://example.com/image.jpg");

            when(groupPurchaseRepository.findById(purchaseId))
                    .thenReturn(Optional.of(groupPurchase));
            when(productRepository.findById(productId))
                    .thenReturn(Optional.of(product));

            // when
            GroupPurchaseDetailInfo result = groupPurchaseService.getGroupPurchaseById(purchaseId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.groupPurchaseId()).isEqualTo(purchaseId);
            assertThat(result.minQuantity()).isEqualTo(10);
            assertThat(result.maxQuantity()).isEqualTo(100);
            assertThat(result.title()).isEqualTo("테스트 공동구매");
            assertThat(result.description()).isEqualTo("테스트 공동구매 설명입니다.");
            assertThat(result.price()).isEqualTo(20000L);
            assertThat(result.discountedPrice()).isEqualTo(15000L);
            assertThat(result.currentQuantity()).isEqualTo(50);
            assertThat(result.sellerId()).isEqualTo(sellerId);
            assertThat(result.productId()).isEqualTo(productId);
            assertThat(result.originalUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(result.category()).isEqualTo(ProductCategory.FOOD);
            assertThat(result.status()).isEqualTo(GroupPurchaseStatus.OPEN);
        }

        @Test
        @DisplayName("존재하지 않는 공동구매를 조회하면 예외가 발생한다")
        void getGroupPurchaseById_groupPurchaseNotFound() {
            // given
            UUID purchaseId = UUID.randomUUID();

            when(groupPurchaseRepository.findById(purchaseId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> groupPurchaseService.getGroupPurchaseById(purchaseId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("공동구매를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("공동구매는 존재하지만 상품이 존재하지 않으면 예외가 발생한다")
        void getGroupPurchaseById_productNotFound() {
            // given
            UUID purchaseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            GroupPurchase groupPurchase = mock(GroupPurchase.class);
            when(groupPurchase.getProductId()).thenReturn(productId);

            when(groupPurchaseRepository.findById(purchaseId))
                    .thenReturn(Optional.of(groupPurchase));
            when(productRepository.findById(productId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> groupPurchaseService.getGroupPurchaseById(purchaseId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }
}
