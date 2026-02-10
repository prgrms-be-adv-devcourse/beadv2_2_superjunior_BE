package store._0982.recommendation.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.recommendation.application.GroupPurchaseAdvisorService;
import store._0982.recommendation.application.dto.PriceQuantityAdvice;
import store._0982.recommendation.presentation.dto.PriceQuantityRequest;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/advisor")
public class AdvisorController {

    private final GroupPurchaseAdvisorService advisorService;

    @PostMapping("/price-quantity")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    public ResponseDto<PriceQuantityAdvice> advisePriceQuantity(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody PriceQuantityRequest request
    ) {
        return new ResponseDto<>(
                HttpStatus.OK,
                advisorService.advisePriceAndQuantity(request.productId()),
                "가격/수량 추천 완료"
        );
    }
}