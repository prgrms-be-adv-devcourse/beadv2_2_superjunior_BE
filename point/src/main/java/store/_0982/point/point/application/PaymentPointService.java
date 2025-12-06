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

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class PaymentPointService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentPointRepository paymentPointRepository;
    private final MemberPointRepository memberPointRepository;
    private final PaymentPointFailureRepository paymentPointFailureRepository;

    public PaymentPointCreateInfo createPaymentPoint(PaymentPointCommand command, UUID memberId) {
        PaymentPoint paymentPoint = PaymentPoint.create(
                // TODO: 추후 프론트(toss-payment.html) 헤더 토큰으로 수정
                memberId,
                command.orderId(),
                command.amount(),
                OffsetDateTime.now()
        );
        return PaymentPointCreateInfo.from(paymentPointRepository.save(paymentPoint));
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

    public PointChargeConfirmInfo confirmPayment(PointChargeConfirmCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        if (paymentPoint.getStatus() == PaymentPointStatus.COMPLETED) {
            throw new CustomException(CustomErrorCode.ALREADY_COMPLETED_PAYMENT);
        }

        TossPaymentResponse tossPaymentResponse = executePaymentConfirmation(command);
        if (!paymentPoint.getOrderId().equals(tossPaymentResponse.orderId())) {
            throw new CustomException(CustomErrorCode.ORDER_ID_MISMATCH);
        }

        paymentPoint.markConfirmed(
                tossPaymentResponse.method(),
                tossPaymentResponse.approvedAt(),
                tossPaymentResponse.paymentKey());

        UUID memberId = paymentPoint.getMemberId();
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseGet(() -> memberPointRepository.save(new MemberPoint(memberId, 0)));

        memberPoint.addPoints(paymentPoint.getAmount());
        return new PointChargeConfirmInfo(
                PaymentPointInfo.from(paymentPoint), MemberPointInfo.from(memberPoint));
    }


    public MemberPointInfo deductPoints(UUID memberId, PointMinusRequest request) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        memberPoint.deductPoints(request.amount());
        return MemberPointInfo.from(memberPoint);
    }

    public PointChargeFailInfo handlePaymentFailure(PointChargeFailCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        if (paymentPoint.getStatus() == PaymentPointStatus.REQUESTED) {
            paymentPoint.markFailed(command.errorMessage());
            paymentPointRepository.save(paymentPoint);
        }

        PaymentPointFailure failure = PaymentPointFailure.from(paymentPoint, command);
        return PointChargeFailInfo.from(paymentPointFailureRepository.save(failure));
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
