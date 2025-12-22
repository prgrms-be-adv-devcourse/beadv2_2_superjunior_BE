package store._0982.commerce.infrastructure.client.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.commerce.infrastructure.client.product.dto.GroupPurchaseFeignDetailInfo;
import store._0982.commerce.infrastructure.client.product.dto.GroupPurchaseFeignInfo;
import store._0982.commerce.infrastructure.client.product.dto.ParticipateFeignInfo;
import store._0982.commerce.infrastructure.client.product.dto.ParticipateFeignRequest;

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
    ResponseDto<GroupPurchaseFeignDetailInfo> getGroupPurchaseById(
            @PathVariable UUID purchaseId
    );

    /**
     * 공동 구매 리스트 조회
     */
    @GetMapping("/internal/purchases")
    ResponseDto<List<GroupPurchaseFeignInfo>> getGroupPurchaseByIds(
            @RequestParam("ids") List<UUID> purchaseIds
    );

    /**
     * 공동 구매 참여(수량 증가)
     */
    @PostMapping("/internal/purchases/{purchaseId}/participate")
    ResponseDto<ParticipateFeignInfo> participate(
            @PathVariable UUID purchaseId,
            @RequestBody ParticipateFeignRequest request
    );
}
