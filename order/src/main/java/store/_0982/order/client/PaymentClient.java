package store._0982.order.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.order.client.dto.MemberPointInfo;
import store._0982.order.client.dto.PointMinusRequest;

import java.util.UUID;

@FeignClient(
        name = "point",
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
}
