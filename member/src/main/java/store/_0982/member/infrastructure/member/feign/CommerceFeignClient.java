package store._0982.member.infrastructure.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.common.dto.ResponseDto;

import java.util.UUID;

@FeignClient(
        name = "commerce-service",
        url = "localhost:8087"
)
public interface CommerceFeignClient {
    @PostMapping(value = "/internal/balances", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseDto<Void> postSellerBalance(@RequestBody UUID sellerId);
}
