package store._0982.commerce.infrastructure.client.payment;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.commerce.infrastructure.client.payment.dto.MemberPointInfo;
import store._0982.commerce.infrastructure.client.payment.dto.PointDeductRequest;
import store._0982.commerce.infrastructure.client.payment.dto.PointReturnRequest;

import java.util.UUID;

@FeignClient(
        name = "point",
        url = "${gateway.host}"
)
public interface PaymentClient {
    /**
     * 포인트 차감
     */
    @PostMapping("/internal/points/deduct")
    ResponseDto<MemberPointInfo> deductPointsInternal(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody PointDeductRequest request
    );

    /**
     * 포인트 환불
     */
    @PostMapping("/internal/points/return")
    ResponseDto<MemberPointInfo> returnPointsInternal(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody PointReturnRequest request
    );

}
