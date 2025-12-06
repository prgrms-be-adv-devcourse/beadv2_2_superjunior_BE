package store._0982.point.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import store._0982.point.common.dto.PageResponse;
import store._0982.point.common.exception.CustomErrorCode;
import store._0982.point.common.exception.CustomException;
import store._0982.point.point.application.dto.*;
import store._0982.point.point.client.TossPaymentClient;
import store._0982.point.point.client.dto.TossPaymentResponse;
import store._0982.point.point.domain.*;
import store._0982.point.point.presentation.dto.PointMinusRequest;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class PaymentPointService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentPointRepository paymentPointRepository;
    private final MemberPointRepository memberPointRepository;
    private final PaymentPointFailureRepository paymentPointFailureRepository;

    // TODO: 같은 orderId로 주문 생성이 동시에 여러 번 요청되었을 때, 낙관적 락이나 비관적 락을 이용할 것인가?
    public PaymentPointCreateInfo createPaymentPoint(PaymentPointCommand command, UUID memberId) {
        return paymentPointRepository.findByOrderId(command.orderId())
                .map(PaymentPointCreateInfo::from)
                .orElseGet(() -> {
                    PaymentPoint paymentPoint = PaymentPoint.create(memberId, command.orderId(), command.amount());
                    return PaymentPointCreateInfo.from(paymentPoint);
                });
    }

    @Transactional(readOnly = true)
    public MemberPointInfo getPoints(UUID memberId) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
        return MemberPointInfo.from(memberPoint);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentPointHistoryInfo> getPaymentHistory(UUID memberId, Pageable pageable) {
        Page<PaymentPointHistoryInfo> page = paymentPointRepository.findAllByMemberId(memberId, pageable)
                .map(PaymentPointHistoryInfo::from);
        return PageResponse.from(page);
    }

    /*
    TODO: 결제 승인 도중 에러가 발생하면, 그냥 에러를 반환해서 클라이언트가 실패 처리를 요청하게 할까,
          아니면 서버에서 바로 실패 처리를 진행할까?
     */
    public PointChargeConfirmInfo confirmPayment(PointChargeConfirmCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        if (paymentPoint.getStatus() == PaymentPointStatus.COMPLETED) {
            throw new CustomException(CustomErrorCode.ALREADY_COMPLETED_PAYMENT);
        }

        TossPaymentResponse tossPaymentResponse = getValidTossPaymentResponse(paymentPoint, command);
        paymentPoint.markConfirmed(tossPaymentResponse.method(), tossPaymentResponse.approvedAt(), tossPaymentResponse.paymentKey());

        UUID memberId = paymentPoint.getMemberId();
        // 처음 결제 승인 시 보유 포인트 0인 객체를 미리 생성
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseGet(() -> memberPointRepository.save(new MemberPoint(memberId, 0)));

        memberPoint.addPoints(paymentPoint.getAmount());
        return new PointChargeConfirmInfo(
                PaymentPointInfo.from(paymentPoint), MemberPointInfo.from(memberPoint));
    }

    // TODO: 한 번만 진행되어야 하는 포인트 차감이 동시에 여러 번 일어나지 않도록 검증이 필요할 것 같다.
    public MemberPointInfo deductPoints(UUID memberId, PointMinusRequest request) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        memberPoint.deductPoints(request.amount());
        return MemberPointInfo.from(memberPoint);
    }

    public PointChargeFailInfo handlePaymentFailure(PointChargeFailCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        switch (paymentPoint.getStatus()) {
            case COMPLETED, REFUNDED -> throw new CustomException(CustomErrorCode.CANNOT_HANDLE_FAILURE);
            case FAILED -> {
                PaymentPointFailure existingFailure = paymentPointFailureRepository.findByPaymentPoint(paymentPoint)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR));
                return PointChargeFailInfo.from(existingFailure);
            }
            case REQUESTED -> paymentPoint.markFailed(command.errorMessage());
        }

        PaymentPointFailure failure = PaymentPointFailure.from(paymentPoint, command);
        return PointChargeFailInfo.from(paymentPointFailureRepository.save(failure));
    }

    private TossPaymentResponse getValidTossPaymentResponse(PaymentPoint paymentPoint, PointChargeConfirmCommand command) {
        TossPaymentResponse tossPaymentResponse = executePaymentConfirmation(command);
        if (!paymentPoint.getOrderId().equals(tossPaymentResponse.orderId())) {
            throw new CustomException(CustomErrorCode.ORDER_ID_MISMATCH);
        }
        return tossPaymentResponse;
    }

    private TossPaymentResponse executePaymentConfirmation(PointChargeConfirmCommand command) {
        try {
            return tossPaymentClient.confirm(command);
        } catch (CustomException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new CustomException(CustomErrorCode.PAYMENT_API_TIMEOUT);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.PAYMENT_API_ERROR);
        }
    }
}
