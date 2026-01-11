package store._0982.batch.infrastructure.client.member;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import store._0982.batch.infrastructure.client.member.dto.ProfileInfo;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountListRequest;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "member-service",
        url = "${client.member}"
)
public interface MemberClient {

    @PostMapping("/internal/members/seller-account")
    ResponseDto<List<SellerAccountInfo>> getSellerAccountInfos(@RequestBody SellerAccountListRequest request);

    @GetMapping("/internal/members/profile")
    ResponseDto<ProfileInfo> getMember(@RequestHeader(value = HeaderName.ID) UUID memberId);

}
