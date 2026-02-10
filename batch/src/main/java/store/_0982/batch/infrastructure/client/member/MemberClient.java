package store._0982.batch.infrastructure.client.member;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import store._0982.batch.infrastructure.client.member.dto.ProfileInfo;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountListRequest;
import store._0982.common.HeaderName;
import store._0982.common.domain.sellerpayout.SellerPayout;
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

    default Map<UUID, SellerAccountInfo> fetchAccounts(List<SellerPayout> sellerPayouts) {
        List<UUID> sellerIds = sellerPayouts.stream()
                .map(SellerPayout::getSellerId)
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

    @Deprecated
    @GetMapping("/internal/members/member-ids")
    ResponseDto<List<UUID>> getMemberIds(@RequestParam int currentPage, @RequestParam int pageSize);

}
