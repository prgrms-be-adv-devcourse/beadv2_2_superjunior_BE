package store._0982.commerce.application.order;

import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.grouppurchase.ParticipateService;
import store._0982.commerce.application.grouppurchase.dto.ParticipateInfo;
import store._0982.commerce.application.order.dto.OrderDetailInfo;
import store._0982.commerce.application.order.dto.OrderRegisterCommand;
import store._0982.commerce.application.order.dto.OrderRegisterInfo;
import store._0982.commerce.domain.cart.CartRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.order.OrderStatus;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.infrastructure.client.member.MemberClient;
import store._0982.commerce.infrastructure.client.member.dto.ProfileInfo;
import store._0982.commerce.infrastructure.client.payment.PaymentClient;
import store._0982.commerce.infrastructure.client.payment.dto.MemberPointInfo;
import store._0982.commerce.infrastructure.client.payment.dto.PointDeductRequest;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;

import javax.xml.stream.FactoryConfigurationError;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private GroupPurchaseRepository groupPurchaseRepository;

    @Mock
    private MemberClient memberClient;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private ParticipateService participateService;

    @Mock
    private GroupPurchaseService groupPurchaseService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getDiscountedPrice()).thenReturn(1000L);
        when(groupPurchase.getStatus()).thenReturn(GroupPurchaseStatus.OPEN);
        when(groupPurchase.getEndDate()).thenReturn(OffsetDateTime.now().plusDays(1));
        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(memberClient.getMember(memberId)).thenReturn(mock(ResponseDto.class));

        when(participateService.participate(any(GroupPurchase.class), anyInt()))
                .thenReturn(ParticipateInfo.success("OPEN",10,"참여 성공"));

        when(paymentClient.deductPointsInternal(
                eq(memberId),
                any(PointDeductRequest.class)
        )).thenReturn(mock(ResponseDto.class));

        OrderRegisterCommand command = new OrderRegisterCommand(
                2,
                "주소",
                " 상세 주소",
                "12345",
                "수령인",
                UUID.randomUUID(),
                purchaseId
        );

        // when
        OrderRegisterInfo result = orderService.createOrder(memberId, command);

        // then
        assertThat(result).isNotNull();
        verify(participateService).participate(any(GroupPurchase.class), eq(2));
    }

    @Test
    @DisplayName("주문 실패 - 회원 없음")
    void createOrder_memberNotFound(){
        // given
        UUID memberId = UUID.randomUUID();

        when(memberClient.getMember(memberId)).thenThrow(FeignException.NotFound.class);

        OrderRegisterCommand command = mock(OrderRegisterCommand.class);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(memberId, command))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("주문 실패 - 공동 구매 없음")
    void createOrder_groupPurchaseNotFound() {
        UUID memberId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();

        when(memberClient.getMember(memberId)).thenReturn(mock(ResponseDto.class));
        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());


        OrderRegisterCommand command = new OrderRegisterCommand(
                1,
                "주소",
                "상세주소",
                "12345",
                "수령인",
                UUID.randomUUID(),
                purchaseId
        );


        // when & then
        assertThatThrownBy(() -> orderService.createOrder(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("공동구매를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 실패 - 포인트 부족 -> CANCELLED")
    void createOrder_pointNotEnough() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();
        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getDiscountedPrice()).thenReturn(1000L);
        when(groupPurchase.getStatus()).thenReturn(GroupPurchaseStatus.OPEN);
        when(groupPurchase.getEndDate()).thenReturn(OffsetDateTime.now().plusDays(1));
        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));

        when(memberClient.getMember(memberId)).thenReturn(mock(ResponseDto.class));
        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        when(paymentClient.deductPointsInternal(any(), any()))
                .thenThrow(new CustomException(CustomErrorCode.LACK_OF_POINT));

        OrderRegisterCommand command = new OrderRegisterCommand(
                2,
                "주소",
                " 상세 주소",
                "12345",
                "수령인",
                UUID.randomUUID(),
                purchaseId
        );

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

        // when
        assertThatThrownBy(() -> orderService.createOrder(memberId, command ))
                .isInstanceOf(CustomException.class);

        // then
        verify(orderRepository, times(2)).save(captor.capture());

        Order lastSavedOrder = captor.getValue();

        assertThat(lastSavedOrder.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("주문 실패 - 참여 실패")
    void createOrder_participateFail() {
        UUID memberId = UUID.randomUUID();
        UUID purchaseId = UUID.randomUUID();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getStatus()).thenReturn(GroupPurchaseStatus.OPEN);
        when(groupPurchase.getEndDate()).thenReturn(OffsetDateTime.now().plusDays(1));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(memberClient.getMember(memberId)).thenReturn(mock(ResponseDto.class));
        when(groupPurchaseRepository.findById(purchaseId)).thenReturn(Optional.of(groupPurchase));

        when(participateService.participate(any(), anyInt()))
                .thenReturn(ParticipateInfo.failure("FULL",0,"마감"));
        when(paymentClient.deductPointsInternal(
                eq(memberId),
                any(PointDeductRequest.class)
        )).thenReturn(mock(ResponseDto.class));

        OrderRegisterCommand command = new OrderRegisterCommand(
                1,"","","","",
                UUID.randomUUID(),
                purchaseId
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(memberId, command))
                .isInstanceOf(CustomException.class);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(2)).save(captor.capture());

        Order savedOrder = captor.getValue();

        assertThat(savedOrder.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void createOrderCart() {
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderById_success() {
        UUID orderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Order order = mock(Order.class);
        when(order.getMemberId()).thenReturn(memberId);
        when(order.getSellerId()).thenReturn(UUID.randomUUID());

        when(orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(Optional.of(order));

        OrderDetailInfo result = orderService.getOrderById(memberId, orderId);

        assertThat(result).isNotNull();
        verify(orderRepository).findByOrderIdAndDeletedAtIsNull(orderId);

    }

    @Test
    void getOrdersBySeller() {
    }

    @Test
    void getOrdersByConsumer() {
    }

//    @Test
//    @DisplayName("주문 상태 변경")
//    void updateOrderStatus_success() {
//        UUID purchaseId = UUID.randomUUID();
//
//        Order order = mock(Order.class);
//        when(orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(purchaseId))
//                .thenReturn(List.of(order));
//
//        orderService.updateOrderStatus(purchaseId, OrderStatus.SUCCESS);
//
//        verify(order).updateStatus(OrderStatus.SUCCESS);
//        verify(orderRepository).saveAll(anyList());
//    }

    @Test
    @DisplayName("주문 환불 처리")
    void returnOrder_success() {
        UUID purchaseId = UUID.randomUUID();

        Order order = mock(Order.class);
        when(order.isReturned()).thenReturn(false);
        when(order.getPrice()).thenReturn(1000L);
        when(order.getQuantity()).thenReturn(2);
        when(order.getMemberId()).thenReturn(UUID.randomUUID());
        when(order.getOrderId()).thenReturn(UUID.randomUUID());

        when(orderRepository.findByGroupPurchaseIdAndStatusAndDeletedAtIsNull(purchaseId, OrderStatus.FAILED))
                .thenReturn(List.of(order));

        when(paymentClient.returnPointsInternal(any(), any()))
                .thenReturn(mock(ResponseDto.class));

        orderService.returnOrder(purchaseId);

        verify(order).markReturned();
        verify(orderRepository).saveAll(anyList());
    }
}