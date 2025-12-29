package store._0982.commerce.application.grouppurchase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseDetailInfo;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseThumbnailInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;

import java.time.OffsetDateTime;
import java.util.List;
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

    @Nested
    @DisplayName("공동구매 목록 조회 Service")
    class GetGroupPurchaseTest {

        @Test
        @DisplayName("공동구매 목록을 페이징하여 조회한다")
        void getGroupPurchase_success() {
            // given
            OffsetDateTime now = OffsetDateTime.now();
            Pageable pageable = PageRequest.of(0, 10);

            GroupPurchase groupPurchase1 = mock(GroupPurchase.class);
            UUID purchaseId1 = UUID.randomUUID();
            UUID productId1 = UUID.randomUUID();
            when(groupPurchase1.getGroupPurchaseId()).thenReturn(purchaseId1);
            when(groupPurchase1.getMinQuantity()).thenReturn(10);
            when(groupPurchase1.getMaxQuantity()).thenReturn(100);
            when(groupPurchase1.getTitle()).thenReturn("공동구매 1");
            when(groupPurchase1.getDiscountedPrice()).thenReturn(15000L);
            when(groupPurchase1.getCurrentQuantity()).thenReturn(50);
            when(groupPurchase1.getStartDate()).thenReturn(now.minusDays(1));
            when(groupPurchase1.getEndDate()).thenReturn(now.plusDays(7));
            when(groupPurchase1.getStatus()).thenReturn(GroupPurchaseStatus.OPEN);
            when(groupPurchase1.getCreatedAt()).thenReturn(now.minusDays(2));
            when(groupPurchase1.getProductId()).thenReturn(productId1);

            GroupPurchase groupPurchase2 = mock(GroupPurchase.class);
            UUID purchaseId2 = UUID.randomUUID();
            UUID productId2 = UUID.randomUUID();
            when(groupPurchase2.getGroupPurchaseId()).thenReturn(purchaseId2);
            when(groupPurchase2.getMinQuantity()).thenReturn(5);
            when(groupPurchase2.getMaxQuantity()).thenReturn(50);
            when(groupPurchase2.getTitle()).thenReturn("공동구매 2");
            when(groupPurchase2.getDiscountedPrice()).thenReturn(25000L);
            when(groupPurchase2.getCurrentQuantity()).thenReturn(30);
            when(groupPurchase2.getStartDate()).thenReturn(now.minusDays(2));
            when(groupPurchase2.getEndDate()).thenReturn(now.plusDays(5));
            when(groupPurchase2.getStatus()).thenReturn(GroupPurchaseStatus.SCHEDULED);
            when(groupPurchase2.getCreatedAt()).thenReturn(now.minusDays(3));
            when(groupPurchase2.getProductId()).thenReturn(productId2);

            Product product1 = mock(Product.class);
            when(product1.getCategory()).thenReturn(ProductCategory.FOOD);

            Product product2 = mock(Product.class);
            when(product2.getCategory()).thenReturn(ProductCategory.ELECTRONICS);

            Page<GroupPurchase> groupPurchasePage = new PageImpl<>(
                    List.of(groupPurchase1, groupPurchase2),
                    pageable,
                    2
            );

            when(groupPurchaseRepository.findAll(pageable))
                    .thenReturn(groupPurchasePage);
            when(productRepository.findById(productId1))
                    .thenReturn(Optional.of(product1));
            when(productRepository.findById(productId2))
                    .thenReturn(Optional.of(product2));

            // when
            PageResponse<GroupPurchaseThumbnailInfo> result
                    = groupPurchaseService.getGroupPurchase(pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.size()).isEqualTo(10);

            GroupPurchaseThumbnailInfo info1 = result.content().get(0);
            assertThat(info1.groupPurchaseId()).isEqualTo(purchaseId1);
            assertThat(info1.title()).isEqualTo("공동구매 1");
            assertThat(info1.category()).isEqualTo(ProductCategory.FOOD);

            GroupPurchaseThumbnailInfo info2 = result.content().get(1);
            assertThat(info2.groupPurchaseId()).isEqualTo(purchaseId2);
            assertThat(info2.title()).isEqualTo("공동구매 2");
            assertThat(info2.category()).isEqualTo(ProductCategory.ELECTRONICS);
        }

        @Test
        @DisplayName("빈 목록을 조회한다")
        void getGroupPurchase_empty() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<GroupPurchase> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(groupPurchaseRepository.findAll(pageable))
                    .thenReturn(emptyPage);

            // when
            PageResponse<GroupPurchaseThumbnailInfo> result
                    = groupPurchaseService.getGroupPurchase(pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
        }

        @Test
        @DisplayName("공동구매는 존재하지만 상품이 없으면 예외가 발생한다")
        void getGroupPurchase_productNotFound() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            UUID productId = UUID.randomUUID();

            GroupPurchase groupPurchase = mock(GroupPurchase.class);
            when(groupPurchase.getProductId()).thenReturn(productId);

            Page<GroupPurchase> groupPurchasePage = new PageImpl<>(
                    List.of(groupPurchase),
                    pageable,
                    1
            );

            when(groupPurchaseRepository.findAll(pageable))
                    .thenReturn(groupPurchasePage);
            when(productRepository.findById(productId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> groupPurchaseService.getGroupPurchase(pageable))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("판매자별 공동구매 목록 조회 Service")
    class GetGroupPurchasesBySellerTest {

        @Test
        @DisplayName("판매자별 공동구매 목록을 페이징하여 조회한다")
        void getGroupPurchasesBySeller_success() {
            // given
            UUID sellerId = UUID.randomUUID();
            OffsetDateTime now = OffsetDateTime.now();
            Pageable pageable = PageRequest.of(0, 10);

            GroupPurchase groupPurchase1 = mock(GroupPurchase.class);
            UUID purchaseId1 = UUID.randomUUID();
            UUID productId1 = UUID.randomUUID();
            when(groupPurchase1.getGroupPurchaseId()).thenReturn(purchaseId1);
            when(groupPurchase1.getMinQuantity()).thenReturn(10);
            when(groupPurchase1.getMaxQuantity()).thenReturn(100);
            when(groupPurchase1.getTitle()).thenReturn("판매자 공동구매 1");
            when(groupPurchase1.getDiscountedPrice()).thenReturn(15000L);
            when(groupPurchase1.getCurrentQuantity()).thenReturn(50);
            when(groupPurchase1.getStartDate()).thenReturn(now.minusDays(1));
            when(groupPurchase1.getEndDate()).thenReturn(now.plusDays(7));
            when(groupPurchase1.getStatus()).thenReturn(GroupPurchaseStatus.OPEN);
            when(groupPurchase1.getCreatedAt()).thenReturn(now.minusDays(2));
            when(groupPurchase1.getProductId()).thenReturn(productId1);

            GroupPurchase groupPurchase2 = mock(GroupPurchase.class);
            UUID purchaseId2 = UUID.randomUUID();
            UUID productId2 = UUID.randomUUID();
            when(groupPurchase2.getGroupPurchaseId()).thenReturn(purchaseId2);
            when(groupPurchase2.getMinQuantity()).thenReturn(5);
            when(groupPurchase2.getMaxQuantity()).thenReturn(50);
            when(groupPurchase2.getTitle()).thenReturn("판매자 공동구매 2");
            when(groupPurchase2.getDiscountedPrice()).thenReturn(25000L);
            when(groupPurchase2.getCurrentQuantity()).thenReturn(30);
            when(groupPurchase2.getStartDate()).thenReturn(now.minusDays(2));
            when(groupPurchase2.getEndDate()).thenReturn(now.plusDays(5));
            when(groupPurchase2.getStatus()).thenReturn(GroupPurchaseStatus.SCHEDULED);
            when(groupPurchase2.getCreatedAt()).thenReturn(now.minusDays(3));
            when(groupPurchase2.getProductId()).thenReturn(productId2);

            Product product1 = mock(Product.class);
            when(product1.getCategory()).thenReturn(ProductCategory.FOOD);

            Product product2 = mock(Product.class);
            when(product2.getCategory()).thenReturn(ProductCategory.ELECTRONICS);

            Page<GroupPurchase> groupPurchasePage = new PageImpl<>(
                    List.of(groupPurchase1, groupPurchase2),
                    pageable,
                    2
            );

            when(groupPurchaseRepository.findAllBySellerId(sellerId, pageable))
                    .thenReturn(groupPurchasePage);
            when(productRepository.findById(productId1))
                    .thenReturn(Optional.of(product1));
            when(productRepository.findById(productId2))
                    .thenReturn(Optional.of(product2));

            // when
            PageResponse<GroupPurchaseThumbnailInfo> result
                    = groupPurchaseService.getGroupPurchasesBySeller(sellerId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.size()).isEqualTo(10);

            GroupPurchaseThumbnailInfo info1 = result.content().get(0);
            assertThat(info1.groupPurchaseId()).isEqualTo(purchaseId1);
            assertThat(info1.title()).isEqualTo("판매자 공동구매 1");
            assertThat(info1.category()).isEqualTo(ProductCategory.FOOD);

            GroupPurchaseThumbnailInfo info2 = result.content().get(1);
            assertThat(info2.groupPurchaseId()).isEqualTo(purchaseId2);
            assertThat(info2.title()).isEqualTo("판매자 공동구매 2");
            assertThat(info2.category()).isEqualTo(ProductCategory.ELECTRONICS);
        }

        @Test
        @DisplayName("해당 판매자의 공동구매가 없으면 빈 목록을 조회한다")
        void getGroupPurchasesBySeller_empty() {
            // given
            UUID sellerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            Page<GroupPurchase> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(groupPurchaseRepository.findAllBySellerId(sellerId, pageable))
                    .thenReturn(emptyPage);

            // when
            PageResponse<GroupPurchaseThumbnailInfo> result
                    = groupPurchaseService.getGroupPurchasesBySeller(sellerId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
        }

        @Test
        @DisplayName("판매자의 공동구매는 존재하지만 상품이 없으면 예외가 발생한다")
        void getGroupPurchasesBySeller_productNotFound() {
            // given
            UUID sellerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            UUID productId = UUID.randomUUID();

            GroupPurchase groupPurchase = mock(GroupPurchase.class);
            when(groupPurchase.getProductId()).thenReturn(productId);

            Page<GroupPurchase> groupPurchasePage = new PageImpl<>(
                    List.of(groupPurchase),
                    pageable,
                    1
            );

            when(groupPurchaseRepository.findAllBySellerId(sellerId, pageable))
                    .thenReturn(groupPurchasePage);
            when(productRepository.findById(productId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> groupPurchaseService.getGroupPurchasesBySeller(sellerId, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }
}
