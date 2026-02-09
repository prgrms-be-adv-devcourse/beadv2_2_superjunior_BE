package store._0982.recommendation.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.recommendation.application.PromptService;
import store._0982.recommendation.application.RecommendationService;
import store._0982.recommendation.presentation.dto.InterestSummaryRequest;
import store._0982.recommendation.presentation.dto.ProductVectorResponse;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RecommendationInternalController {

    private final PromptService promptService;
    private final RecommendationService recommendationService;

    @PostMapping("/internal/recommendations/interest-summary")
    public String summarizeInterest(@RequestBody InterestSummaryRequest request){
        return promptService.summarizeInterest(request.descriptions());
    }

    @GetMapping("/internal/recommendations/product-vectors/{productId}")
    public ResponseDto<ProductVectorResponse> getProductVector(@PathVariable UUID productId) {
        float[] vector = recommendationService.getProductVector(productId);
        return new ResponseDto<>(HttpStatus.OK, new ProductVectorResponse(vector), "상품 벡터 검색 완료");
    }
}
