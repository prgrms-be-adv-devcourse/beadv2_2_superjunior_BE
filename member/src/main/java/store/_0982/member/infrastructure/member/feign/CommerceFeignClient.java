package store._0982.member.infrastructure.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.common.dto.ResponseDto;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "commerce-service",
        url = "localhost:8087"
)
public interface CommerceFeignClient {
    @PostMapping(value = "/internal/balances", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseDto<Void> postSellerBalance(@RequestBody UUID sellerId);

    // TODO: 참여자 ID 목록을 조회할 수 있는 API가 필요하다
    @GetMapping("/{groupPurchaseId}/participants")
    List<UUID> getGroupPurchaseParticipants(@PathVariable UUID groupPurchaseId);
}
