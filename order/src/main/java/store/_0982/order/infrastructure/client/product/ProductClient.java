package store._0982.order.infrastructure.client.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.order.infrastructure.client.product.dto.GroupPurchaseDetailInfo;
import store._0982.order.infrastructure.client.product.dto.GroupPurchaseInfo;
import store._0982.order.infrastructure.client.product.dto.ParticipateInfo;
import store._0982.order.infrastructure.client.product.dto.ParticipateRequest;

import java.util.List;
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
     * 공동 구매 리스트 조회
     */
    @GetMapping("/internal/purchases")
    ResponseDto<List<GroupPurchaseInfo>> getGroupPurchaseByIds(
            @RequestParam("ids") List<UUID> purchaseIds
    );

    /**
     * 공동 구매 참여(수량 증가)
     */
    @PostMapping("/internal/purchases/{purchaseId}/participate")
    ResponseDto<ParticipateInfo> participate(
            @PathVariable UUID purchaseId,
            @RequestBody ParticipateRequest request
    );
}
