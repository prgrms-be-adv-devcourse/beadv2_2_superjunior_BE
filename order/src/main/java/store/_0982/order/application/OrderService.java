package store._0982.order.application;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.*;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.order.application.dto.OrderDetailInfo;
import store._0982.order.application.dto.OrderInfo;
import store._0982.order.application.dto.OrderRegisterCommand;
import store._0982.order.application.dto.OrderRegisterInfo;
import store._0982.order.client.MemberClient;
import store._0982.order.client.PaymentClient;
import store._0982.order.client.ProductClient;
import store._0982.order.client.dto.*;
import store._0982.order.domain.OrderRepository;
import store._0982.order.domain.Order;
import store._0982.order.domain.OrderStatus;
import store._0982.order.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberClient memberClient;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;

    /**
     * 주문 생성
     * @param memberId 고객
     * @param command 주문 command
     * @return OrderRegisterInfo
     */
    @Transactional
    public OrderRegisterInfo createOrder(UUID memberId, OrderRegisterCommand command) {

        // 주문자 존재 여부
        validateMember(memberId);

        // 공동 구매 존재 여부
        validateGroupPurchase(command.groupPurchaseId());

        // 공동 구매 참여
        ParticipateRequest participateRequest = new ParticipateRequest(command.quantity());
        ParticipateInfo participateInfo = productClient.participate(
                command.groupPurchaseId(),
                participateRequest
        ).data();

        if (!participateInfo.success()) {
            throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_REACHED);
        }

        // 포인트 차감 (예치금)
        deductPoints(memberId, command);

        // order 생성
        Order order = Order.create(command, memberId);

        Order savedOrder = orderRepository.save(order);
        return OrderRegisterInfo.from(savedOrder);
    }

    private void validateMember(UUID memberId){
        try{
            memberClient.getProfile(memberId);
        }catch(FeignException.NotFound e){
            throw new CustomException(CustomErrorCode.MEMBER_NOT_FOUND);
        }catch(FeignException e){
            log.error("회원 조회 실패 : memberId={}, status={}", memberId, e.status());
            throw new RuntimeException("회원 정보 조회 중 오류가 발생했습니다.");
        }
    }

    private void validateGroupPurchase(UUID groupPurchaseId){
        try{
            // ResponseDto로 받기
            ResponseDto<GroupPurchaseDetailInfo> response =
                    productClient.getGroupPurchaseById(groupPurchaseId);
            GroupPurchaseDetailInfo groupPurchase = response.data();

            // 상태확인
            if(groupPurchase.status() != GroupPurchaseStatus.OPEN){
                throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_NOT_OPEN);
            }

            // 종료시간 확인
            if(groupPurchase.endDate().isBefore(OffsetDateTime.now())){
                throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_END);
            }

        }catch(CustomException e){
            // CustomException은 그대로 던지기
            throw e;

        }catch(FeignException.NotFound e){
            log.error("공동구매 없음: groupPurchaseId={}", groupPurchaseId);
            throw new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND);

        }catch(FeignException e){
            log.error("공동구매 조회 실패: groupPurchaseId={}, status={}, message={}",
                    groupPurchaseId, e.status(), e.getMessage());
            throw new RuntimeException("공동 구매 정보 조회 중 오류가 발생했습니다.");
        }
    }

    private void deductPoints(UUID memberId, OrderRegisterCommand command){
        int totalAmount = command.price() * command.quantity();
        try{
            paymentClient.deductPointsInternal(
                    memberId,
                    new PointMinusRequest(totalAmount)
            );
        }catch(FeignException e){
            log.error("포인트 차감 실패: memberId={}, amount={}", memberId, totalAmount, e);

            // TODO: 포인트 차감 실패 시 공동 구매 참여 롤백
        }
    }

    /**
     * 주문 상세 조회
     * @param requesterID 요청자
     * @param orderId 주문 id
     * @return OrderDetailInfo
     */
    public OrderDetailInfo getOrderById(UUID requesterID, UUID orderId) {
        Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        if(!order.getMemberId().equals(requesterID)
            && !order.getSellerId().equals(requesterID)){
            throw new CustomException(CustomErrorCode.ORDER_ACCESS_DENIED);
        }

        return OrderDetailInfo.from(order);
    }

    /**
     * 판매자별 주문 목록 조회
     * @param sellerId 판매자 id
     * @param pageable pageable
     * @return OrderInfo
     */
    public PageResponse<OrderInfo> getOrdersBySeller(UUID sellerId, Pageable pageable) {
        Page<Order> orders = orderRepository.findBySellerIdAndDeletedIsNull(sellerId,pageable);

        Page<OrderInfo> orderInfos = orders.map(OrderInfo::from);
        return PageResponse.from(orderInfos);
    }

    /**
     * 판매자별 주문 목록 조회
     * @param memberId 구매자 id
     * @param pageable pageable
     * @return OrderInfo
     */
    public PageResponse<OrderInfo> getOrdersByConsumer(UUID memberId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByMemberIdAndDeletedIsNull(memberId,pageable);

        Page<OrderInfo> orderInfos = orders.map(OrderInfo::from);
        return PageResponse.from(orderInfos);
    }

    /**
     * 주문 상태 변경
     * @param groupPurchaseId 공동구매 id
     * @param orderStatus 주문 상태
     */
    @Transactional
    public void updateOrderStatus(UUID groupPurchaseId, OrderStatus orderStatus) {
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(groupPurchaseId);
        log.info("orderStatus : {}",orderStatus);
        for(Order order : orders){
            log.info("orderId : {}",order.getOrderId());
            order.updateStatus(orderStatus);
        }

        orderRepository.saveAll(orders);
    }

    @Transactional
    public void returnOrder(UUID groupPurchaseId){
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndStatusAndDeletedAtIsNull(groupPurchaseId, OrderStatus.FAILED);
        log.info("환불주문");
        int success = 0;
        int fail = 0;
        for(Order order : orders){

            if(order.isReturned()){
                log.info("이미 환불된 주문 : {}", order.getOrderId());
                continue;
            }
            int amount = order.getPrice() * order.getQuantity();

            try{
                paymentClient.returnPointsInternal(
                        order.getMemberId(),
                        new PointReturnRequest(amount)
                );
                order.markReturned();
                success++;
                log.info("환불 완료 : orderId = {}, memberId = {}, amount = {}", order.getOrderId(), order.getMemberId(), amount);
            }catch(Exception e){
                fail++;
                // TODO : 환불 실패 처리 필요(재시도 큐? 관리자 알람?)
            }
        }
    }

}
