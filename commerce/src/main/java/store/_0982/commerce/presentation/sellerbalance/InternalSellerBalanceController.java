package store._0982.commerce.presentation.sellerbalance;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.sellerbalance.SellerBalanceService;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceThumbnailInfo;
import store._0982.commerce.presentation.sellerbalance.dto.SellerBalanceRequest;
import store._0982.common.dto.ResponseDto;

@RequestMapping("/internal/balances")
@RequiredArgsConstructor
@RestController
public class InternalSellerBalanceController {

    private final SellerBalanceService sellerBalanceService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public ResponseDto<SellerBalanceThumbnailInfo> createSellerBalance(
            @Valid @RequestBody SellerBalanceRequest sellerBalanceRequest) {
        SellerBalanceThumbnailInfo info = sellerBalanceService.createSellerBalance(sellerBalanceRequest.toCommand());
        return new ResponseDto<>(HttpStatus.OK, info, "seller balance 생성되었습니다.");
    }
}
