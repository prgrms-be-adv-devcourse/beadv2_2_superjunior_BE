package store._0982.member.infrastructure.commerce;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "commerce-service", path = "/internal/group-purchase")
public interface CommerceClient {

    // TODO: 참여자 ID 목록을 조회할 수 있는 API가 필요하다
    @GetMapping("/{groupPurchaseId}/participants")
    List<UUID> getGroupPurchaseParticipants(@PathVariable UUID groupPurchaseId);
}
