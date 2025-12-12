package store._0982.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.common.dto.ResponseDto;
import store._0982.order.client.dto.GroupPurchaseDetailInfo;
import store._0982.order.client.dto.ParticipateInfo;
import store._0982.order.client.dto.ParticipateRequest;

import java.util.UUID;

@FeignClient(
        name = "product",
        url = "${gateway.host}"
)
public interface ProductClient {

    /**
     * 공동 구매 조회
     */
    @GetMapping("/api/purchases/{purchaseId}")
    ResponseDto<GroupPurchaseDetailInfo> getGroupPurchaseById(
            @PathVariable UUID purchaseId
    );

    /**
     * 공동 구매 참여(수량 증가)
     */
    @PostMapping("/api/purchases/{purchaseId}/participate")
    ResponseDto<ParticipateInfo> participate(
            @PathVariable UUID purchaseId,
            @RequestBody ParticipateRequest request
    );
}
