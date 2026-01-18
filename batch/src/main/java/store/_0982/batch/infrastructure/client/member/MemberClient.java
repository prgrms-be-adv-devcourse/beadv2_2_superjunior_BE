package store._0982.batch.infrastructure.client.member;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.batch.infrastructure.client.member.dto.ProfileInfo;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountListRequest;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@FeignClient(
        name = "member-service",
        url = "${client.member}"
)
public interface MemberClient {

    @PostMapping("/internal/members/seller-account")
    ResponseDto<List<SellerAccountInfo>> getSellerAccountInfos(@RequestBody SellerAccountListRequest request);

    @GetMapping("/internal/members/profile")
    ResponseDto<ProfileInfo> getMember(@RequestHeader(value = HeaderName.ID) UUID memberId);

    default Map<UUID, SellerAccountInfo> fetchAccounts(List<Settlement> settlements) {
        List<UUID> sellerIds = settlements.stream()
                .map(Settlement::getSellerId)
                .toList();

        SellerAccountListRequest request = new SellerAccountListRequest(sellerIds);

        ResponseDto<List<SellerAccountInfo>> response = getSellerAccountInfos(request);
        if (response == null || response.data() == null) {
            return Collections.emptyMap();
        }

        return response.data()
                .stream()
                .collect(Collectors.toMap(SellerAccountInfo::sellerId, Function.identity()));
    }

}
