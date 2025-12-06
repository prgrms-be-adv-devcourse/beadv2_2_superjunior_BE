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
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId());
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
        MemberPoint afterPayment = MemberPoint.plusPoint(saved.getMemberId(), prevPoint.getPointBalance()+saved.getAmount());
        MemberPoint afterPoint = memberPointRepository.save(afterPayment);
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

        memberPoint.minus(pointBalance);
        memberPointRepository.save(memberPoint);
        return MemberPointInfo.from(memberPoint);
    }

    public PointChargeFailInfo pointPaymentFail(PointChargeFailCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId());
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
}
