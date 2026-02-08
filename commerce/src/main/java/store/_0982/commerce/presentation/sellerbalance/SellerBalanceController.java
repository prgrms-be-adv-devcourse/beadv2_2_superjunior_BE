package store._0982.commerce.presentation.sellerbalance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.commerce.application.sellerbalance.SellerBalanceService;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceHistoryInfo;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceInfo;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/balances")
@RestController
@Tag(name = "Seller Balance", description = "판매자 정산 잔액 및 변동 내역 API")
public class SellerBalanceController {

    private final SellerBalanceService sellerBalanceService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    @Operation(summary = "판매자 정산 잔액 조회", description = "판매자의 현재 정산 가능한 잔액을 조회합니다.")
    public ResponseDto<SellerBalanceInfo> getBalance(
            @RequestHeader(HeaderName.ID) UUID memberId
            ) {
        SellerBalanceInfo info = sellerBalanceService.getBalance(memberId);
        return new ResponseDto<>(HttpStatus.OK, info, "조회되었습니다.");
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/history")
    @Operation(summary = "판매자 정산 잔액 변동 내역 조회", description = "판매자의 정산 잔액 변동 이력을 페이징하여 조회합니다.")
    public ResponseDto<PageResponse<SellerBalanceHistoryInfo>> processDailySettlement(
            @RequestHeader(HeaderName.ID) UUID memberId,
            Pageable pageable
    ) {
        PageResponse<SellerBalanceHistoryInfo> info = sellerBalanceService.getBalanceHistory(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, info, "조회되었습니다.");
    }
}
