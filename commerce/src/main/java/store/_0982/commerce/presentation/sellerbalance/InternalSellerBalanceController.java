package store._0982.commerce.presentation.sellerbalance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.sellerbalance.SellerBalanceService;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceThumbnailInfo;
import store._0982.commerce.presentation.sellerbalance.dto.SellerBalanceRequest;
import store._0982.common.dto.ResponseDto;

@Tag(name = "Internal Seller Balance", description = "내부용 판매자 잔액 API")
@RequestMapping("/internal/balances")
@RequiredArgsConstructor
@RestController
public class InternalSellerBalanceController {

    private final SellerBalanceService sellerBalanceService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    @Operation(summary = "판매자 잔액 생성 (내부용)", description = "내부 시스템에서 판매자 잔액 정보를 생성합니다.")
    public ResponseDto<SellerBalanceThumbnailInfo> createSellerBalance(
            @Valid @RequestBody SellerBalanceRequest sellerBalanceRequest) {
        SellerBalanceThumbnailInfo info = sellerBalanceService.createSellerBalance(sellerBalanceRequest.toCommand());
        return new ResponseDto<>(HttpStatus.OK, info, "seller balance 생성되었습니다.");
    }
}
