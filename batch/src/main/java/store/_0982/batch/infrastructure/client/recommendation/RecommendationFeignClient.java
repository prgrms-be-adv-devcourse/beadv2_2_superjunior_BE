package store._0982.batch.infrastructure.client.recommendation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.batch.infrastructure.client.recommendation.dto.InterestSummaryRequest;

@FeignClient(
        name = "recommendation-service",
        url = "${client.recommendation:http://localhost:8088}"
)
public interface RecommendationFeignClient {
    @PostMapping(value = "/internal/recommendation/interest-summary")
    String summarizeInterest(@RequestBody InterestSummaryRequest request);
}
