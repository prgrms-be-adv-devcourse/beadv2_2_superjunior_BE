package store._0982.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.order.client.dto.SellerAccountInfo;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "member-service", url = "${gateway.host}")
public interface MemberFeignClient {

    @GetMapping("/internal/members/seller-accounts")
    List<SellerAccountInfo> getSellerAccountInfos(@RequestBody List<UUID> sellerIds);

}
