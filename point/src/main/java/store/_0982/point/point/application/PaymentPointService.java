package store._0982.point.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import store._0982.point.common.dto.PageResponse;
import store._0982.point.common.exception.CustomErrorCode;
import store._0982.point.common.exception.CustomException;
import store._0982.point.point.application.dto.*;
import store._0982.point.point.client.dto.TossPaymentClient;
import store._0982.point.point.client.dto.TossPaymentResponse;
import store._0982.point.point.domain.*;

import org.springframework.data.domain.Pageable;
import store._0982.point.point.presentation.dto.PointMinusRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 토스 결제 관리
 * 포인트 관리
 */

@RequiredArgsConstructor
@Service
public class PaymentPointService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentPointRepository paymentPointRepository;
    private final MemberPointRepository memberPointRepository;
    private final PaymentPointFailureRepository paymentPointFailureRepository;

    /**
     * 상품 정보 조회
     * @param memberId 멤버 id
     * @param command orderId, amount
     * @return PaymentPointCreateInfo
     */
    public PaymentPointCreateInfo pointPaymentCreate(PaymentPointCommand command, UUID memberId) {
        if(memberId == null){
            throw new CustomException(CustomErrorCode.NO_LOGIN_INFO);
        }

        if (!memberPointRepository.existsById(memberId)) {
            MemberPoint newMember = new MemberPoint(memberId, 0);
            memberPointRepository.save(newMember);
        }

        if (command.amount() <= 0) {
            throw new CustomException(CustomErrorCode.INVALID_AMOUNT);
        }
        PaymentPoint paymentPoint = PaymentPoint.create(
                //todo 추후 프론트(toss-payment.html) 헤더 토큰으로 수정
                memberId,
                command.orderId(),
                command.amount(),
                OffsetDateTime.now()
        );
        PaymentPoint requested = paymentPointRepository.save(paymentPoint);
        return PaymentPointCreateInfo.from(requested);
    }

    public MemberPointInfo pointCheck(UUID memberId) {
        if(memberId == null){
            throw new CustomException(CustomErrorCode.NO_LOGIN_INFO);
        }
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
        return MemberPointInfo.from(memberPoint);
    }

    public PageResponse<PaymentPointHistoryInfo> paymentHistoryFind(UUID memberId, Pageable pageable) {
        if(memberId == null){
            throw new CustomException(CustomErrorCode.NO_LOGIN_INFO);
        }
        if (!memberPointRepository.existsById(memberId)) {
            throw new CustomException(CustomErrorCode.HISTORY_NOT_FOUND);
        }
        Page<PaymentPointHistoryInfo> page =
                paymentPointRepository.findAllByMemberId(memberId, pageable)
                        .map(PaymentPointHistoryInfo::from);
        if(page.isEmpty()){
            throw new CustomException(CustomErrorCode.PAGE_NOT_FOUND);
        }
        return  PageResponse.from(page);
    }

    public PointChargeConfirmInfo pointPaymentConfirm(PointChargeConfirmCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));
        if (paymentPoint == null) {
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND);
        }
        if(command.paymentKey() == null){
            throw new CustomException(CustomErrorCode.PAYMENT_KEY_ISNULL);
        }
        if(command.amount() != paymentPoint.getAmount()){
            throw new CustomException(CustomErrorCode.DIFFERENT_AMOUNT);
        }
        if(paymentPoint.getStatus() == PaymentPointStatus.COMPLETED){
            throw new CustomException(CustomErrorCode.COMPLETED_PAYMENT);
        }
        TossPaymentResponse tossPayment;
        try{
            tossPayment = tossPaymentClient.confirm(command);
        }catch (Exception e){
            throw new CustomException(CustomErrorCode.PAYMENT_COMPLETE_FAILED);
        }

        OffsetDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt() : null;
        paymentPoint.markConfirmed(
                tossPayment.method(),
                approvedAt,
                tossPayment.paymentKey());

        if (!paymentPoint.getOrderId().equals(tossPayment.orderId())) {
            throw new CustomException(CustomErrorCode.ORDER_ID_MISMATCH);
        }
        PaymentPoint saved = paymentPointRepository.save(paymentPoint);
        MemberPoint prevPoint = memberPointRepository.findById(saved.getMemberId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
        prevPoint.plusPoint(saved.getMemberId(), prevPoint.getPointBalance()+saved.getAmount());
        MemberPoint afterPoint = memberPointRepository.save(prevPoint);
        return new PointChargeConfirmInfo(
                PaymentPointInfo.from(saved), MemberPointInfo.from(afterPoint));
    }

    public MemberPointInfo pointMinus(UUID memberId, PointMinusRequest request) {
        if(memberId == null){
            throw new CustomException(CustomErrorCode.NO_LOGIN_INFO);
        }
        if(request.amount() <= 0){
            throw new CustomException(CustomErrorCode.INVALID_AMOUNT);
        }
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        int pointBalance = memberPoint.getPointBalance() - request.amount();

        if(pointBalance < 0){
            throw new CustomException(CustomErrorCode.LACK_OF_POINT);
        }

        memberPoint.minus(pointBalance, OffsetDateTime.now());
        memberPointRepository.save(memberPoint);
        return MemberPointInfo.from(memberPoint);
    }

    public PointChargeFailInfo pointPaymentFail(PointChargeFailCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));
        if(paymentPoint.getStatus() == PaymentPointStatus.REQUESTED){
            paymentPoint.markFailed(command.errorMessage());
            paymentPointRepository.save(paymentPoint);
        }
        PaymentPointFailure failure = PaymentPointFailure.from(
                paymentPoint.getId(),
                command.orderId(),
                command.paymentKey(),
                command.errorCode(),
                command.errorMessage(),
                command.amount(),
                command.rawPayload()
        );
        PaymentPointFailure saved = paymentPointFailureRepository.save(failure);
        return PointChargeFailInfo.from(saved);
    }

    public PointRefundInfo pointRefund(UUID memberId, PointRefundCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));
        if (paymentPoint == null) {
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND);
        }
        if(paymentPoint.getStatus() != PaymentPointStatus.COMPLETED){
            throw new CustomException(CustomErrorCode.NOT_COMPLTED_PAYMENT);
        }
        //todo 추후 헤더값으로 수정
        if(!paymentPoint.getMemberId().equals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))){
            throw new CustomException(CustomErrorCode.PAYMENT_OWNER_MISMATCH);
        }
        MemberPoint memberPoint = memberPointRepository.findById(paymentPoint.getMemberId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        OffsetDateTime paymentAt = paymentPoint.getApprovedAt();
        OffsetDateTime lastUsedAt = memberPoint.getLastUsedAt();

        if (lastUsedAt != null && lastUsedAt.isAfter(paymentAt)) {
            throw new CustomException(CustomErrorCode.REFUND_AFTER_ORDER);
        }

        TossPaymentResponse response = tossPaymentClient.cancel(paymentPoint.getPaymentKey(), command.cancelReason(), paymentPoint.getAmount());
        paymentPoint.markRefunded(response.cancels().get(0).canceledAt(), response.cancels().get(0).cancelReason());
        paymentPointRepository.save(paymentPoint);
        int pointBalance = memberPoint.getPointBalance() - response.cancels().get(0).cancelAmount();
        memberPoint.refund(pointBalance);
        memberPointRepository.save(memberPoint);
        return PointRefundInfo.from(paymentPoint);
    }
}
