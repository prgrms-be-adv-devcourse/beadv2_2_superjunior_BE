package store._0982.commerce.application.order;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.ParticipateService;
import store._0982.commerce.application.grouppurchase.dto.ParticipateInfo;
import store._0982.commerce.application.order.dto.OrderRegisterCommand;
import store._0982.commerce.application.order.dto.OrderRegisterInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.order.OrderStatus;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.infrastructure.client.member.MemberClient;
import store._0982.commerce.infrastructure.client.member.dto.ProfileInfo;
import store._0982.commerce.infrastructure.client.payment.PaymentClient;
import store._0982.commerce.infrastructure.client.payment.dto.PointDeductRequest;
import store._0982.commerce.infrastructure.order.OrderJpaRepository;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.dto.GroupPurchaseChangedEvent;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class OrderServiceIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private MemberClient memberClient;

    @MockitoBean
    private PaymentClient paymentClient;

    @MockitoBean
    private ParticipateService participateService;

    @MockitoBean
    private KafkaTemplate<String, GroupPurchaseEvent> upsertKafkaTemplate;

    @MockitoBean
    private KafkaTemplate<String, GroupPurchaseChangedEvent> notificationKafkaTemplate;

    private ExecutorService executorService;

    private UUID testMemberId;
    private UUID testSellerId;
    private UUID testProductId;
    private GroupPurchase testGroupPurchase;

    @BeforeEach
    void setUp(){
        testMemberId = UUID.randomUUID();
        testSellerId = UUID.randomUUID();

        // Product 생성
        Product testProduct = createTestProduct();
        productRepository.save(testProduct);
        testProductId = testProduct.getProductId();

        // GroupPurchase 생성
        testGroupPurchase = createTestGroupPurchase();
        groupPurchaseRepository.save(testGroupPurchase);
        testGroupPurchase.updateStatus(GroupPurchaseStatus.OPEN);
        groupPurchaseRepository.save(testGroupPurchase);

        // MemberClient
        ProfileInfo profileInfo = new ProfileInfo(
                testMemberId,
                "test@example.com",
                "테스트유저",
                OffsetDateTime.now(),
                "CUSTOMER",
                null,
                "010-1234-5678"
        );
        ResponseDto<ProfileInfo> response = new ResponseDto<>(
                HttpStatus.OK,
                profileInfo,
                "조회 성공"
        );
        when(memberClient.getMember(testMemberId))
                .thenReturn(response);

    }

    @Test
    @Transactional
    @DisplayName("통합테스트 - 주문을 생성합니다")
    void createOrder_integration_success() {
        // given
        when(participateService.participate(any(GroupPurchase.class), anyInt()))
                .thenReturn(ParticipateInfo.success("OPEN", 10,"참여 성공"));

        when(paymentClient.deductPointsInternal(
                eq(testMemberId),
                any(PointDeductRequest.class)
        )).thenReturn(new ResponseDto<>(200, null, "success"));

        OrderRegisterCommand command = new OrderRegisterCommand(
                2,
                "서울시 강서구",
                "101동 101호",
                "12345",
                "홍길동",
                testMemberId,
                testGroupPurchase.getGroupPurchaseId()
        );

        // when
        OrderRegisterInfo response = orderService.createOrder(testMemberId, command);

        // then
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isNotNull();

        Order savedOrder = orderRepository.findById(response.orderId())
                .orElseThrow();

        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(savedOrder.getQuantity()).isEqualTo(2);
        assertThat(savedOrder.getMemberId()).isEqualTo(testMemberId);
        assertThat(savedOrder.getReceiverName()).isEqualTo("홍길동");

        verify(participateService).participate(any(GroupPurchase.class), eq(2));
        verify(paymentClient).deductPointsInternal(eq(testMemberId), any(PointDeductRequest.class));

    }

    @Test
    @Transactional
    @DisplayName("통합테스트 - 참여 실패 시 주문 취소")
    void createOrder_integration_participateFailed_CancelOrder() {
        // given
        when(participateService.participate(any(GroupPurchase.class), anyInt()))
                .thenReturn(ParticipateInfo.failure("OPEN", 0,"마감"));

        OrderRegisterCommand command = new OrderRegisterCommand(
                2,
                "서울시 강서구",
                "101동 101호",
                "12345",
                "홍길동",
                testMemberId,
                testGroupPurchase.getGroupPurchaseId()
        );

        log.info("테스트 시작 - participateInfo.success() 확인 필요");
        // when & then
        assertThatThrownBy(() -> orderService.createOrder(testMemberId, command))
               .isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", CustomErrorCode.GROUP_PURCHASE_IS_REACHED);


        // DB에서 CANCELLED 상태로 저장되었는지 확인
        Order cancelledOrder = orderJpaRepository.findAll().stream()
                .filter(order -> order.getGroupPurchaseId().equals(testGroupPurchase.getGroupPurchaseId()))
                .findFirst()
                .orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        // 포인트 차감이 호출되지 않았는지 확인
        verify(paymentClient, never()).deductPointsInternal(any(), any());

    }

    @Test
    @Transactional
    @DisplayName("통함테스트 - 포인트 부족 시 보상 트랜잭션 보상")
    void createOrder_integration_lackOfPoint_CancelOrder(){
        // given
        when(participateService.participate(any(GroupPurchase.class),anyInt()))
                .thenReturn(ParticipateInfo.success("OPEN", 10, "참여 성공"));

        when(paymentClient.deductPointsInternal(any(), any()))
                .thenThrow(FeignException.BadRequest.class);

        OrderRegisterCommand command = new OrderRegisterCommand(
                2,
                "서울시 강서구",
                "101동 101호",
                "12345",
                "홍길동",
                testMemberId,
                testGroupPurchase.getGroupPurchaseId()
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(testMemberId, command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomErrorCode.LACK_OF_POINT);

        // 보상 트랜잭션 확인
        verify(participateService).cancelParticipate(testGroupPurchase.getGroupPurchaseId(), command.quantity());

        // DB에서 CANCELLED 상태로 저장되었는지 확인
        Order cancelledOrder = orderJpaRepository.findAll().stream()
                .filter(order -> order.getGroupPurchaseId().equals(testGroupPurchase.getGroupPurchaseId()))
                .findFirst()
                .orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

    }


    private Product createTestProduct() {
        return new Product(
                "테스트 상품",
                20000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                null,
                testSellerId
        );
    }

    private GroupPurchase createTestGroupPurchase(){
        return new GroupPurchase(
                10,
                100,
                "테스트 공동구매",
                "테스트 공동구매 설명",
                12000L,
                OffsetDateTime.now().plusHours(1),
                OffsetDateTime.now().plusDays(7),
                testSellerId,
                testProductId
        );
    }
}
