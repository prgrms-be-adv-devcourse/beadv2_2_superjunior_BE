package store._0982.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import store._0982.common.HeaderName;
import store._0982.product.client.dto.ProfileInfo;
import store._0982.product.common.dto.ResponseDto;

import java.util.UUID;

@FeignClient(
        name = "memberClient",
        url = "${gateway.host}"
)
public interface MemberClient {
    @GetMapping("/internal/members/profile")
    ResponseDto<ProfileInfo> getMember(@RequestHeader(value = HeaderName.ID) UUID memberId);
}
