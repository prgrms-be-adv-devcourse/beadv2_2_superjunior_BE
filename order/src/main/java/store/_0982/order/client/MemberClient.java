package store._0982.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import store._0982.common.dto.ResponseDto;
import store._0982.order.client.dto.ProfileInfo;

import java.util.UUID;

@FeignClient(
        name = "member",
        url = "${gateway.host}"
)
public interface MemberClient {
    /**
     * 회원 프로필 조회(존재 여부 확인용)
     */
    @GetMapping("/api/members/profile")
    ResponseDto<ProfileInfo> getProfile(@RequestHeader("X-Member-Id") UUID memberId);
}
