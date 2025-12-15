package store._0982.order.presentation.sellerbalance;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.auth.RequireRole;
import store._0982.common.auth.Role;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.order.application.sellerbalance.SellerBalanceService;
import store._0982.order.application.sellerbalance.dto.SellerBalanceHistoryInfo;
import store._0982.order.application.sellerbalance.dto.SellerBalanceInfo;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/balances")
@RestController
public class SellerBalanceController {

    private final SellerBalanceService sellerBalanceService;

    @RequireRole({Role.SELLER, Role.ADMIN})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<SellerBalanceInfo> getBalance(
            @RequestHeader(HeaderName.ID) UUID memberId
            ) {
        SellerBalanceInfo info = sellerBalanceService.getBalance(memberId);
        return new ResponseDto<>(HttpStatus.OK, info, "조회되었습니다.");
    }

    @RequireRole({Role.SELLER, Role.ADMIN})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/history")
    public ResponseDto<PageResponse<SellerBalanceHistoryInfo>> getBalanceHistory(
            @RequestHeader(HeaderName.ID) UUID memberId,
            Pageable pageable
    ) {
        PageResponse<SellerBalanceHistoryInfo> info = sellerBalanceService.getBalanceHistory(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, info, "조회되었습니다.");
    }

}
