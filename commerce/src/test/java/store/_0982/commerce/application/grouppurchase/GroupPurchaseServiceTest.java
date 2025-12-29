package store._0982.commerce.application.grouppurchase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import store._0982.commerce.application.grouppurchase.dto.*;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.infrastructure.client.member.MemberClient;
import store._0982.commerce.infrastructure.client.member.dto.ProfileInfo;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

import javax.swing.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupPurchaseServiceTest {

    @Mock
    private GroupPurchaseRepository groupPurchaseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MemberClient memberClient;

    @Mock
    private KafkaTemplate<String, GroupPurchaseEvent> upsertKafkaTemplate;

    @Mock
    ResponseDto<ProfileInfo> responseDto;

    @InjectMocks
    private GroupPurchaseService groupPurchaseService;

    private UUID memberId;
    private Product product;
    private GroupPurchaseRegisterCommand registerCommand;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();

        product = new Product(
                "테스트 상품",
                20000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "상품 url",
                memberId
        );

        registerCommand = new GroupPurchaseRegisterCommand(
                10,
                100,
                "테스트 공동구매",
                "테스트 공동구매 설명",
                12000L,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(7),
                product.getProductId()
        );
    }

    @Test
    @DisplayName("공동 구매를 생성합니다.")
    void createGroupPurchase_success() {
        // given
        when(productRepository.findById(product.getProductId()))
                .thenReturn(Optional.of(product));

        GroupPurchase saved = new GroupPurchase(
                registerCommand.minQuantity(),
                registerCommand.maxQuantity(),
                registerCommand.title(),
                registerCommand.description(),
                registerCommand.discountedPrice(),
                registerCommand.startDate(),
                registerCommand.endDate(),
                memberId,
                product.getProductId()

        );
        OffsetDateTime productNow = OffsetDateTime.now();

        ReflectionTestUtils.setField(product, "createdAt", productNow);
        ReflectionTestUtils.setField(product, "updatedAt", productNow);
//
//        ReflectionTestUtils.setField(saved, "createdAt", now);
//        ReflectionTestUtils.setField(saved, "updatedAt", now);

        when(groupPurchaseRepository.saveAndFlush(any(GroupPurchase.class)))
                .thenAnswer(invocation -> {
                    GroupPurchase gp = invocation.getArgument(0);
                    OffsetDateTime now = OffsetDateTime.now();
                    ReflectionTestUtils.setField(gp, "createdAt", now);
                    ReflectionTestUtils.setField(gp, "updatedAt", now);
                    return gp;
                });

        ProfileInfo profileInfo = new ProfileInfo(
                memberId,
                "test@test.com",
                "판매자이름",
                OffsetDateTime.now(),
                "SELLER",
                "imageUrl",
                "010-1234-5678"
        );

        when(responseDto.data()).thenReturn(profileInfo);
        when(memberClient.getMember(memberId)).thenReturn(responseDto);

        // when
        GroupPurchaseInfo result = groupPurchaseService.createGroupPurchase(memberId, registerCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(registerCommand.title());

        verify(memberClient).getMember(memberId);
        verify(groupPurchaseRepository).saveAndFlush(any(GroupPurchase.class));
        verify(upsertKafkaTemplate).send(
                eq(KafkaTopics.GROUP_PURCHASE_CREATED),
                anyString(),
                any(GroupPurchaseEvent.class)
        );

    }

    @Test
    @DisplayName("상품이 미존재하는 경우 공동 구매 생성 시 에러를 반환합니다.")
    void createGroupPurchase_productNotFound() {
        when(productRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                groupPurchaseService.createGroupPurchase(memberId, registerCommand)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("상품의 주인이 아닌 경우 공동 구매 생성 시 에러를 반환합니다.")
    void createGroupPurchase_notOwner(){
        Product otherSellerProduct = mock(Product.class);
        when(otherSellerProduct.getSellerId()).thenReturn(UUID.randomUUID());

        when(productRepository.findById(any()))
                .thenReturn(Optional.of(otherSellerProduct));

        assertThatThrownBy(() ->
                groupPurchaseService.createGroupPurchase(memberId, registerCommand)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("본인이 등록한 상품이 아닙니다.");
    }

    @Test
    @DisplayName("공동 구매 수량 확인 후 minQuantity 보다 maxQuantity 크면 예외처리 합니다.")
    void createGroupPurchase_invalidQuantityRange(){
        when(productRepository.findById(product.getProductId()))
                .thenReturn(Optional.of(product));

        GroupPurchaseRegisterCommand invalid =
                new GroupPurchaseRegisterCommand(
                        100,
                        10,

                        "수량 오류 테스트",
                        "수량 오류 테스트 설명",
                        10000L,
                        OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now().plusDays(7),
                        product.getProductId()
                );


        assertThatThrownBy(() ->
                groupPurchaseService.createGroupPurchase(memberId, invalid)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("잘못된 수량입니다.");
    }

    @Test
    @DisplayName("공동구매의 시작일이 종료일보다 늦을 경우에 예외처리 합니다.")
    void createGroupPurchase_invalidDateRange(){
        when(productRepository.findById(product.getProductId()))
                .thenReturn(Optional.of(product));

        GroupPurchaseRegisterCommand invalid =
                new GroupPurchaseRegisterCommand(
                        10,
                        100,

                        "수량 오류 테스트",
                        "수량 오류 테스트 설명",
                        10000L,
                        OffsetDateTime.now().plusDays(7),
                        OffsetDateTime.now().plusDays(1),
                        product.getProductId()
                );

        assertThatThrownBy(() ->
                groupPurchaseService.createGroupPurchase(memberId, invalid)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("잘못된 날짜 범위입니다.");
    }

//    @Test
//    @DisplayName("회원 조회 실패")
//    void createGroupPurchase_memberClientFail() {
//        when(memberClient.getMember(any()))
//                .thenThrow(new RuntimeException("Feign error"));
//
//        assertThatThrownBy(() ->
//                groupPurchaseService.createGroupPurchase(memberId, registerCommand)
//        ).isInstanceOf(RuntimeException.class);
//
//    }


    @Test
    @DisplayName("공동구매를 Id로 조회합니다.")
    void getGroupPurchaseById_success() {
        // given
        UUID purchaseId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        UUID productId = UUID.randomUUID();

        when(groupPurchase.getProductId()).thenReturn(productId);

        Product product = mock(Product.class);
        when(product.getOriginalUrl()).thenReturn("imageUrl");
        when(product.getPrice()).thenReturn(20000L);
        when(product.getCategory()).thenReturn(ProductCategory.BEAUTY);

        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // when
        GroupPurchaseDetailInfo result = groupPurchaseService.getGroupPurchaseById(purchaseId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.originalUrl()).isEqualTo("imageUrl");
        assertThat(result.price()).isEqualTo(20000L);
        assertThat(result.category()).isEqualTo(ProductCategory.BEAUTY);

        verify(groupPurchaseRepository).findById(purchaseId);
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("공동구매 미존재시 공동구매 Id 조회 에러를 반환합니다.")
    void getGroupPurchaseById_groupPurchaseNotFound() {
        UUID purchaseId = UUID.randomUUID();

        when(groupPurchaseRepository.findById(purchaseId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(()->
                groupPurchaseService.getGroupPurchaseById(purchaseId)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("공동구매를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("상품 미존재시 공동구매 Id 조회 에러를 반환합니다.")
    void getGroupPurchaseById_productNotFound() {
        UUID purchaseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getProductId()).thenReturn(productId);
        when(groupPurchaseRepository.findById(purchaseId))
                .thenReturn(Optional.of(groupPurchase));
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupPurchaseService.getGroupPurchaseById(purchaseId)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다.");

        verify(groupPurchaseRepository).findById(purchaseId);
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("공동 구매 리스트를 조회힙니다.")
    void getGroupPurchase_success() {
        // given
        Pageable pageable = PageRequest.of(0,2);

        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        GroupPurchase groupPurchase1 = mock(GroupPurchase.class);
        GroupPurchase groupPurchase2 = mock(GroupPurchase.class);

        when(groupPurchase1.getProductId()).thenReturn(productId1);
        when(groupPurchase2.getProductId()).thenReturn(productId2);

        Page<GroupPurchase> page = new PageImpl<>(List.of(groupPurchase1,groupPurchase2), pageable, 2);

        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);

        when(product1.getCategory()).thenReturn(ProductCategory.BEAUTY);
        when(product2.getCategory()).thenReturn(ProductCategory.ELECTRONICS);

        when(groupPurchaseRepository.findAll(pageable)).thenReturn(page);
        when(productRepository.findById(productId1)).thenReturn(Optional.of(product1));
        when(productRepository.findById(productId2)).thenReturn(Optional.of(product2));

        // when
        PageResponse<GroupPurchaseThumbnailInfo> result = groupPurchaseService.getGroupPurchase(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).category()).isEqualTo(ProductCategory.BEAUTY);
        assertThat(result.content().get(1).category()).isEqualTo(ProductCategory.ELECTRONICS);

        verify(groupPurchaseRepository).findAll(pageable);
        verify(productRepository).findById(productId1);
        verify(productRepository).findById(productId2);
    }

    @Test
    @DisplayName("상품 미존재시 공동구매 목록 조회를 실패합니다.")
    void getGroupPurchase_productNotFound(){
        // given
        Pageable pageable = PageRequest.of(0,1);

        UUID productId = UUID.randomUUID();
        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getProductId()).thenReturn(productId);

        Page<GroupPurchase> page = new PageImpl<>(List.of(groupPurchase), pageable, 1);

        when(groupPurchaseRepository.findAll(pageable)).thenReturn(page);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                groupPurchaseService.getGroupPurchase(pageable)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("공동 구매가 하나도 없을 경우 빈 리스트로 출력합니다.")
    void getGroupPurchase_empty(){
        // given
        Pageable pageable = PageRequest.of(0,10);
        Page<GroupPurchase> page = Page.empty(pageable);

        when(groupPurchaseRepository.findAll(pageable)).thenReturn(page);

        // when
        PageResponse<GroupPurchaseThumbnailInfo> result = groupPurchaseService.getGroupPurchase(pageable);

        // then
        assertThat(result.content()).isEmpty();
        verify(groupPurchaseRepository).findAll(pageable);
        verify(productRepository, never()).findById(any());
    }

    @Test
    @DisplayName("공동구매 ID 목록으로 조회합니다.")
    void getGroupPurchaseByIds_success() {
        // given
        UUID purchase1 = UUID.randomUUID();
        UUID purchase2 = UUID.randomUUID();

        List<UUID> purchaseIds = List.of(purchase1, purchase2);

        GroupPurchase groupPurchase1 = mock(GroupPurchase.class);
        GroupPurchase groupPurchase2 = mock(GroupPurchase.class);

        List<GroupPurchase> groupPurchases = List.of(groupPurchase1, groupPurchase2);

        when(groupPurchaseRepository.findAllByGroupPurchaseIdIn(purchaseIds)).thenReturn(groupPurchases);

        // when
        List<GroupPurchase> result = groupPurchaseService.getGroupPurchaseByIds(purchaseIds);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(groupPurchase1,groupPurchase2);

        verify(groupPurchaseRepository, times(1)).findAllByGroupPurchaseIdIn(purchaseIds);
    }

    @Test
    @DisplayName("판매자의 공동구매 목록을 조회합니다.")
    void getGroupPurchasesBySeller_success() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0,10);

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getProductId()).thenReturn(productId);

        Product product = mock(Product.class);
        when(product.getCategory()).thenReturn(ProductCategory.ELECTRONICS);

        Page<GroupPurchase> page = new PageImpl<>(List.of(groupPurchase), pageable, 1);

        when(groupPurchaseRepository.findAllBySellerId(sellerId, pageable)).thenReturn(page);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // when
        PageResponse<GroupPurchaseThumbnailInfo> result = groupPurchaseService.getGroupPurchasesBySeller(sellerId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).category()).isEqualTo(ProductCategory.ELECTRONICS);

        verify(groupPurchaseRepository, times(1)).findAllBySellerId(sellerId, pageable);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("판매자 공동구매 조회 시 상품이 없을 경우에 에러를 반환합니다.")
    void getGroupPurchaseBySeller_productNotFound(){
        // given
        UUID sellerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Pageable pageable = PageRequest.of(0,10);

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getProductId()).thenReturn(productId);

        Page<GroupPurchase> page = new PageImpl<>(List.of(groupPurchase), pageable, 1);

        when(groupPurchaseRepository.findAllBySellerId(sellerId, pageable)).thenReturn(page);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                groupPurchaseService.getGroupPurchasesBySeller(sellerId, pageable)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("공동구매를 삭제합니다.")
    void deleteGroupPurchase_success() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);

        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));
        when(groupPurchase.getStatus()).thenReturn(GroupPurchaseStatus.SCHEDULED);
        when(groupPurchase.getSellerId()).thenReturn(memberId);

        GroupPurchaseEvent mockEvent = mock(GroupPurchaseEvent.class);
        when(mockEvent.getId()).thenReturn(purchaseId);

        when(groupPurchase.toEvent(
                anyString(),
                eq(GroupPurchaseEvent.SearchKafkaStatus.DELETE_GROUP_PURCHASE),
                isNull()
        )).thenReturn(mockEvent);

        // when
        groupPurchaseService.deleteGroupPurchase(purchaseId, memberId);

        // then
        verify(groupPurchaseRepository).delete(groupPurchase);
        verify(upsertKafkaTemplate).send(
                eq(KafkaTopics.GROUP_PURCHASE_CHANGED),
                eq(purchaseId.toString()),
                any(GroupPurchaseEvent.class)
        );
    }

    @Test
    @DisplayName("공동구매 미존재 시 공동 구매 삭제를 실패합니다.")
    void deleteGroupPurchase_notFound() {
        // given
        UUID purchaseId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                groupPurchaseService.deleteGroupPurchase(purchaseId, memberId)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("공동구매를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("공동구매 상태가 SCHEDULED가 상태가 아닐 경우엔 공동구매 삭제를 실패합니다.")
    void deleteGroupPurchase_notScheduled() {
        // given
        UUID purchaseId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));
        when(groupPurchase.getStatus()).thenReturn(GroupPurchaseStatus.FAILED);

        // when & then
        assertThatThrownBy(() -> groupPurchaseService. deleteGroupPurchase(purchaseId, memberId)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("공동 구매가 OPEN 상태입니다.");
    }

    @Test
    @DisplayName("판매자 아닌 경우에는 공동구매 삭제를 실패합니다.")
    void deleteGroupPurchase_notSeller(){
        // given
        UUID purchaseId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID notSellerId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchaseRepository.findById(purchaseId))
                .thenReturn(Optional.of(groupPurchase));
        when(groupPurchase.getStatus()).thenReturn(GroupPurchaseStatus.SCHEDULED);
        when(groupPurchase.getSellerId()).thenReturn(sellerId);

        // when & then
        assertThatThrownBy(() -> groupPurchaseService.deleteGroupPurchase(purchaseId, notSellerId)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("본인이 등록한 공동구매만 삭제할 수 있습니다.");
        verify(groupPurchaseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("공동 구매를 업데이트 합니다.")
    void updateGroupPurchase_success() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));
        when(groupPurchase.getSellerId()).thenReturn(memberId);
        when(groupPurchase.getStatus()).thenReturn(GroupPurchaseStatus.SCHEDULED);
        when(groupPurchase.getProductId()).thenReturn(productId);

        GroupPurchaseUpdateCommand command = new GroupPurchaseUpdateCommand(
                10, 100, "수정 제목", "수정 설명",
                12000L,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(7),
                productId
        );

        when(groupPurchaseRepository.saveAndFlush(groupPurchase))
                .thenReturn(groupPurchase);

        Product product = mock(Product.class);
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(product.getSellerId()).thenReturn(memberId);
        when(product.toEvent()).thenReturn(null);

        ResponseDto<ProfileInfo> response = mock(ResponseDto.class);
        when(memberClient.getMember(memberId)).thenReturn(response);
        when(response.data()).thenReturn(new ProfileInfo(memberId, "", "판매자", null, null, null, null));

        GroupPurchaseEvent event = mock(GroupPurchaseEvent.class);
        when(event.getId()).thenReturn(productId);
        when(groupPurchase.toEvent(anyString(), any(), any()))
                .thenReturn(event);

        // when
        GroupPurchaseInfo result = groupPurchaseService.updateGroupPurchase(memberId, purchaseId, command);

        // then
        assertThat(result).isNotNull();
        verify(groupPurchase).updateGroupPurchase(
                command.minQuantity(),
                command.maxQuantity(),
                command.title(),
                command.description(),
                command.discountedPrice(),
                command.startDate(),
                command.endDate(),
                command.productId()
        );
        verify(upsertKafkaTemplate).send(
                eq(KafkaTopics.GROUP_PURCHASE_CHANGED),
                anyString(),
                any(GroupPurchaseEvent.class)
        );
    }

    @Test
    @DisplayName("본인이 등록하지 않은 공동구매의 수정을 실패합니다.")
    void updateGroupPurchase_notSeller() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        UUID otherSellerId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));
        when(groupPurchase.getSellerId()).thenReturn(sellerId);

        // when & then
        assertThatThrownBy(() -> groupPurchaseService.updateGroupPurchase(memberId, purchaseId, mock(GroupPurchaseUpdateCommand.class))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("본인이 등록한 공동구매만 삭제할 수 있습니다.");

    }

    @Test
    @DisplayName("미정산 공동구매 목록 조회합니다.")
    void getUnsettledGroupPurchases_success() {
        // given
        GroupPurchase groupPurchase1 = mock(GroupPurchase.class);
        GroupPurchase groupPurchase2 = mock(GroupPurchase.class);

        when(groupPurchaseRepository.findByStatusAndSettledAtIsNull(GroupPurchaseStatus.SUCCESS))
                .thenReturn(List.of(groupPurchase1, groupPurchase2));

        // when
        List<GroupPurchaseInternalInfo> result = groupPurchaseService.getUnsettledGroupPurchases();

        //then
        assertThat(result).hasSize(2);
        verify(groupPurchaseRepository)
                .findByStatusAndSettledAtIsNull(GroupPurchaseStatus.SUCCESS);
    }

    @Test
    @DisplayName("미정산 공동구매 정산 처리합니다.")
    void markAsSettled_success() {
        // given
        UUID purchaseId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));
        when(groupPurchase.isSettled()).thenReturn(false);

        // when
        groupPurchaseService.markAsSettled(purchaseId);

        // then
        verify(groupPurchase).markAsSettled();
        verify(groupPurchaseRepository).save(groupPurchase);
    }

    @Test
    @DisplayName("이미 정산된 공동구매인지 확인합니다.")
    void markAsSettled_alreadySettled(){
        // given
        UUID purchaseId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));
        when(groupPurchase.isSettled()).thenReturn(true);

        // when
        groupPurchaseService.markAsSettled(purchaseId);

        // then
        verify(groupPurchase, never()).markAsSettled();
        verify(groupPurchaseRepository, never()).save(any());
    }
}