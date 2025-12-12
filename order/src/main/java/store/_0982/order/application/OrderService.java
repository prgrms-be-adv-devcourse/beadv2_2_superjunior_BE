package store._0982.order.application;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.order.application.dto.OrderRegisterCommand;
import store._0982.order.application.dto.OrderRegisterInfo;
import store._0982.order.client.MemberClient;
import store._0982.order.client.PaymentClient;
import store._0982.order.client.ProductClient;
import store._0982.order.client.dto.*;
import store._0982.order.domain.Order;
import store._0982.order.domain.OrderRepository;
import store._0982.order.exception.CustomErrorCode;

import java.time.OffsetDateTime;
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
     *
     * @param memberId 고객
     * @param command  주문 command
     * @return OrderRegisterInfo
     */
    @Transactional
    public OrderRegisterInfo createOrder(UUID memberId, OrderRegisterCommand command) {

        // 주문자 존재 여부
        validateMember(memberId);

        // 공동 구매 존재 여부
        validateGroupPurchase(command.groupPurchaseId());

        // 공동 구매 참여
        participate(command);

        // 포인트 차감 (예치금)
        deductPoints(memberId, command);

        // order 생성
        Order order = Order.create(command, memberId);

        Order savedOrder = orderRepository.save(order);
        return OrderRegisterInfo.from(savedOrder);
    }

    private void validateMember(UUID memberId) {
        try {
            memberClient.getProfile(memberId);
        } catch (FeignException.NotFound e) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_FOUND);
        } catch (FeignException e) {
            log.error("회원 조회 실패 : memberId={}, status={}", memberId, e.status());
            throw new RuntimeException("회원 정보 조회 오류");
        }
    }

    private void validateGroupPurchase(UUID groupPurchaseId) {
        try {
            // ResponseDto로 받기
            ResponseDto<GroupPurchaseDetailInfo> response =
                    productClient.getGroupPurchaseById(groupPurchaseId);
            GroupPurchaseDetailInfo groupPurchase = response.data();

            // 상태확인
            if (groupPurchase.status() != GroupPurchaseStatus.OPEN) {
                throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_NOT_OPEN);
            }

            // 종료시간 확인
            if (groupPurchase.endDate().isBefore(OffsetDateTime.now())) {
                throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_END);
            }

        } catch (CustomException e) {
            throw e;

        } catch (FeignException.NotFound e) {
            log.error("공동구매 없음: groupPurchaseId={}", groupPurchaseId);
            throw new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND);

        } catch (FeignException e) {
            log.error("공동구매 조회 실패: groupPurchaseId={}, message={}", groupPurchaseId, e.getMessage());
            throw new RuntimeException("공동 구매 정보 조회 오류 발생");
        }
    }

    private void participate(OrderRegisterCommand command) {
        // 공동 구매 참여
        ParticipateRequest participateRequest = new ParticipateRequest(command.quantity());
        ParticipateInfo participateInfo = productClient.participate(
                command.groupPurchaseId(),
                participateRequest
        ).data();

        if (!participateInfo.success()) {
            throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_REACHED);
        }

    }

    private void deductPoints(UUID memberId, OrderRegisterCommand command) {
        int totalAmount = command.price() * command.quantity();
        try {
            paymentClient.deductPointsInternal(
                    memberId,
                    new PointMinusRequest(totalAmount)
            );
        } catch (FeignException e) {
            log.error("포인트 차감 실패: memberId={}, amount={}", memberId, totalAmount, e);

            // TODO: 포인트 차감 실패 시 공동 구매 참여 롤백
        }
    }

}
