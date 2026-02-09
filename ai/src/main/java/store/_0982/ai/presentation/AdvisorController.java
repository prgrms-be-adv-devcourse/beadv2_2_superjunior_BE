package store._0982.ai.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import store._0982.ai.application.GroupPurchaseAdvisorService;
import store._0982.ai.application.dto.PriceQuantityAdvice;
import store._0982.ai.presentation.dto.PriceQuantityRequest;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;

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
