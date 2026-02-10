package store._0982.recommendation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.recommendation.application.dto.AdvisorVectorSearchRequest;
import store._0982.recommendation.application.dto.PriceQuantityAdvice;
import store._0982.recommendation.application.dto.VectorSearchResponse;
import store._0982.recommendation.infrastructure.feign.commerce.CommerceFeignClient;
import store._0982.recommendation.infrastructure.feign.commerce.dto.GroupPurchasePerformanceInfo;
import store._0982.recommendation.infrastructure.feign.commerce.dto.ProductDetailInfo;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupPurchaseAdvisorService {
    private final CommerceFeignClient commerceFeignClient;
    private final SearchQueryPort searchQueryPort;
    private final EmbeddingService embeddingService;
    private final GroupPurchaseAdvisorPromptService advisorPromptService;

    /**
     * 가격 / 수량 추천
     *
     * 1. 상품 정보 가져오기
     * 2. 상품 벡터 가져오기
     * 3. 벡터로 비슷한 과거 공구 검색
     * 4. 과거 공구들의 성과 데이터 가져오기
     * 5. LLM에게 전달하여 추천받기
     */
    public PriceQuantityAdvice advisePriceAndQuantity(UUID productId){
        // 상품 정보
        ProductDetailInfo product = commerceFeignClient.getProduct(productId);

        // 상품 벡터
        String content = product.name() + " " + product.description() + " " + product.category();
        float[] vector = embeddingService.embedText(content);

        List<VectorSearchResponse> similar = searchQueryPort.getAdvisorCandidates(
                new AdvisorVectorSearchRequest(
                        "",
                        product.category().name(),
                        vector,
                        10,
                        List.of("OPEN", "SUCCESS", "FAILED")
                )
        );

        if (similar.isEmpty()) {
            Long originalPrice = product.price();
            Long discountedPrice = originalPrice == null ? 0L : Math.round(originalPrice * 0.75);
            return new PriceQuantityAdvice(
                    discountedPrice,
                    10,
                    50,
                    25.0,
                    7,
                    "유사 공구 데이터가 없어 기본 정책(할인율 25%, 최소 10/최대 50, 7일)을 적용했습니다.",
                    0.0,
                    0,
                    List.of()
            );
        }

        List<UUID> groupPurchaseIds = similar.stream()
                .map(VectorSearchResponse::groupPurchaseId)
                .toList();

        List<GroupPurchasePerformanceInfo> performanceInfos = commerceFeignClient.getPerformance(groupPurchaseIds);

        return advisorPromptService.generatePriceQuantityAdvice(product, performanceInfos);

    }

}