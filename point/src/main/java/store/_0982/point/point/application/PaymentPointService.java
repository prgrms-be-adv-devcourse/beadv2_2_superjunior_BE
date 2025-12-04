package store._0982.point.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import store._0982.point.common.dto.ResponseDto;
import store._0982.point.point.application.dto.MemberPointInfo;
import store._0982.point.point.application.dto.PaymentPointCommand;
import store._0982.point.point.application.dto.PaymentPointInfo;
import store._0982.point.point.client.TossPaymentProperties;
import store._0982.point.point.client.dto.TossPaymentClient;
import store._0982.point.point.client.dto.TossPaymentResponse;
import store._0982.point.point.domain.MemberPoint;
import store._0982.point.point.domain.MemberPointRepository;
import store._0982.point.point.domain.PaymentPoint;
import store._0982.point.point.domain.PaymentPointRepository;
import store._0982.point.point.presentation.dto.PointChargeCreateRequest;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PaymentPointService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentPointRepository paymentPointRepository;
    private final MemberPointRepository memberPointRepository;

    public ResponseDto<PaymentPointInfo> createPointPayment(PaymentPointCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);
        PaymentPoint paymentPoint = PaymentPoint.create(
                UUID.randomUUID(),  //멤버id 나중에 수정
                tossPayment.paymentKey(),
                tossPayment.orderId(),
                tossPayment.amount()
        );
        OffsetDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt() : null;
        OffsetDateTime requestedAt = tossPayment.requestedAt() != null ? tossPayment.requestedAt() : null;
        paymentPoint.markConfirmed(tossPayment.method(), approvedAt, requestedAt);

        PaymentPoint saved = paymentPointRepository.save(paymentPoint);
        return new ResponseDto<>(HttpStatus.CREATED, PaymentPointInfo.from(saved),"결제 성공");
    }

    public ResponseDto<MemberPointInfo> pointCheck(UUID memberId) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("memberId not found: " + memberId));
        return new ResponseDto<>(HttpStatus.OK, MemberPointInfo.from(memberPoint), "포인트 조회 성공");
    }
}
