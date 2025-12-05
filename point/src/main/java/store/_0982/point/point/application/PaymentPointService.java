package store._0982.point.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import store._0982.point.common.dto.PageResponse;
import store._0982.point.common.dto.ResponseDto;
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
    public ResponseDto<PaymentPointCreateInfo> pointPaymentCreate(PaymentPointCommand command, UUID memberId) {
        if(memberId == null){
            throw new CustomException(CustomErrorCode.NO_LOGIN_INFO, "로그인 정보가 없습니다.");
        }

        if (!memberPointRepository.existsById(memberId)) {
            MemberPoint newMember = new MemberPoint(memberId, 0);
            memberPointRepository.save(newMember);
        }

        if (command.amount() <= 0) {
            throw new CustomException(CustomErrorCode.INVALID_AMOUNT, "잘못된 충전 금액입니다.");
        }
        PaymentPoint paymentPoint = PaymentPoint.create(
                //todo 추후 프론트(toss-payment.html) 헤더 토큰으로 수정
                memberId,
                command.orderId(),
                command.amount(),
                OffsetDateTime.now()
        );
        PaymentPoint requested = paymentPointRepository.save(paymentPoint);
        return new ResponseDto<>(HttpStatus.CREATED.value(), PaymentPointCreateInfo.from(requested),"결제 요청 생성");

    }

    public ResponseDto<MemberPointInfo> pointCheck(UUID memberId) {
        if(memberId == null){
            throw new CustomException(CustomErrorCode.NO_LOGIN_INFO, "로그인 정보가 없습니다.");
        }
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND, "멤버를 찾을 수 없습니다."));
        return new ResponseDto<>(HttpStatus.OK.value(), MemberPointInfo.from(memberPoint), "포인트 조회 성공");
    }

    public ResponseDto<PageResponse<PaymentPointHistoryInfo>> paymentHistoryFind(UUID memberId, Pageable pageable) {
        if(memberId == null){
            throw new CustomException(CustomErrorCode.NO_LOGIN_INFO, "로그인 정보가 없습니다.");
        }
        if (!memberPointRepository.existsById(memberId)) {
            throw new CustomException(CustomErrorCode.HISTORY_NOT_FOUND, "포인트 충전 내역이 없습니다.");
        }
        Page<PaymentPointHistoryInfo> page =
                paymentPointRepository.findAllByMemberId(memberId, pageable)
                        .map(PaymentPointHistoryInfo::from);
        if(page.isEmpty()){
            throw new CustomException(CustomErrorCode.MEMBER_NOT_FOUND, "잘못된 페이지 번호입니다.");
        }
        return  new ResponseDto<>(HttpStatus.OK.value(), PageResponse.from(page), "포인트 충전 내역 조회 성공");
    }

    public ResponseDto<PointChargeConfirmInfo> pointPaymentConfirm(PointChargeConfirmCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId());
        if (paymentPoint == null) {
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND, "결제 요청을 찾을 수 없습니다.");
        }
        if(command.paymentKey() == null){
            throw new CustomException(CustomErrorCode.PAYMENT_KEY_ISNULL, "paymentKey는 필수입니다.");
        }
        if(command.amount() != paymentPoint.getAmount()){
            throw new CustomException(CustomErrorCode.DIFFERENT_AMOUNT, "결제 금액이 불일치합니다.");
        }
        if(paymentPoint.getStatus() == PaymentPointStatus.COMPLETED){
            throw new CustomException(CustomErrorCode.COMPLETED_PAYMENT, "이미 완료된 결제입니다.");
        }
        TossPaymentResponse tossPayment;
        try{
            tossPayment = tossPaymentClient.confirm(command);
        }catch (Exception e){
            throw new CustomException(CustomErrorCode.PAYMENT_COMPLETE_FAILED, "결제 승인 중 오류가 발생했습니다.");
        }

        OffsetDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt() : null;
        paymentPoint.markConfirmed(
                tossPayment.method(),
                approvedAt,
                tossPayment.paymentKey());

        if (!paymentPoint.getOrderId().equals(tossPayment.orderId())) {
            throw new CustomException(CustomErrorCode.ORDER_ID_MISMATCH, "주문 번호가 일치하지 않습니다.");
        }
        PaymentPoint saved = paymentPointRepository.save(paymentPoint);
        MemberPoint prevPoint = memberPointRepository.findById(saved.getMemberId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND, "멤버를 찾을 수 없습니다."));
        MemberPoint afterPayment = MemberPoint.plusPoint(saved.getMemberId(), prevPoint.getPointBalance()+saved.getAmount());
        MemberPoint afterPoint = memberPointRepository.save(afterPayment);
        PointChargeConfirmInfo pointChargeConfirmInfo = new PointChargeConfirmInfo(
                PaymentPointInfo.from(saved), MemberPointInfo.from(afterPoint)
        );
        return new ResponseDto<>(HttpStatus.CREATED.value(), pointChargeConfirmInfo,"결제 및 포인트 충전 성공");
    }

    public ResponseDto<MemberPointInfo> pointMinus(UUID memberId, PointMinusRequest request) {
        if(memberId == null){
            throw new CustomException(CustomErrorCode.NO_LOGIN_INFO, "로그인 정보가 없습니다.");
        }
        if(request.amount() <= 0){
            throw new CustomException(CustomErrorCode.INVALID_AMOUNT, "잘못된 차감 금액입니다.");
        }
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND, "멤버를 찾을 수 없습니다."));

        int pointBalance = memberPoint.getPointBalance() - request.amount();

        if(pointBalance < 0){
            throw new CustomException(CustomErrorCode.LACK_OF_POINT, "보유 포인트가 부족합니다.");
        }

        memberPoint.minus(pointBalance);
        memberPointRepository.save(memberPoint);
        return new ResponseDto<>(HttpStatus.OK.value(), MemberPointInfo.from(memberPoint), "포인트 차감 완료");
    }

    public ResponseDto<PointChargeFailInfo> pointPaymentFail(PointChargeFailCommand command) {
        PaymentPointFailure failure = PaymentPointFailure.from(
                command.orderId(),
                command.paymentKey(),
                command.errorCode(),
                command.errorMessage(),
                command.amount(),
                command.rawPayload()
        );
        PaymentPointFailure saved = paymentPointFailureRepository.save(failure);
        return new ResponseDto<>(HttpStatus.CREATED.value(), PointChargeFailInfo.from(saved), "결제 실패 정보 저장 완료");
    }
}
