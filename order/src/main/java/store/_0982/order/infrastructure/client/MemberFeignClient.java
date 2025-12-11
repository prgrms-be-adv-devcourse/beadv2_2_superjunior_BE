package store._0982.order.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.order.infrastructure.client.dto.SellerAccountInfo;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "member-service", url = "${feign.member.url}")
public interface MemberFeignClient {

    @GetMapping("/api/members/internal/seller-accounts")
    List<SellerAccountInfo> getSellerAccountInfos(@RequestBody List<UUID> sellerIds);

}
