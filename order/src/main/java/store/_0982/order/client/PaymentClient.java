package store._0982.order.client;


import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.order.client.dto.MemberPointInfo;
import store._0982.order.client.dto.PointMinusRequest;
import store._0982.order.client.dto.PointReturnRequest;

import java.util.UUID;

@FeignClient(
        name = "payment",
        url = "${gateway.host}"
)
public interface PaymentClient {
    /**
     * 포인트 차감
     */
    @PostMapping("/api/points/internal/deduct")
    ResponseDto<MemberPointInfo> deductPointsInternal(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody PointMinusRequest request
    );

    /**
     * 포인트 환불
     */
    @PostMapping("/api/points/internal/return")
    ResponseDto<MemberPointInfo> returnPointsInternal(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody PointReturnRequest request
    );

}
