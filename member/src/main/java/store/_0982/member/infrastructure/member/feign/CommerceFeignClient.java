package store._0982.member.infrastructure.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.common.dto.ResponseDto;
import store._0982.member.infrastructure.member.feign.dto.SellerBalanceRequest;

@FeignClient(
        name = "commerce-service",
        url = "localhost:8087"
)
public interface CommerceFeignClient {
    @PostMapping(value = "/internal/balances")
    ResponseDto<Void> postSellerBalance(@RequestBody SellerBalanceRequest sellerBalanceRequest);
}
