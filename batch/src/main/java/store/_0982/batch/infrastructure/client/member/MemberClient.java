package store._0982.batch.infrastructure.client.member;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountListRequest;
import store._0982.common.dto.ResponseDto;

import java.util.List;

@FeignClient(
        name = "member",
        url = "${gateway.host}"
)
public interface MemberClient {

    @PostMapping("/internal/members/seller-account")
    ResponseDto<List<SellerAccountInfo>> getSellerAccountInfos(@RequestBody SellerAccountListRequest request);

}
