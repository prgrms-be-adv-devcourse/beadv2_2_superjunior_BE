package store._0982.batch.infrastructure.client.ai;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.batch.infrastructure.client.ai.dto.InterestSummaryRequest;

@FeignClient(
        name = "ai-service",
        url = "${client.ai:http://localhost:8088}"
)
public interface AiFeignClient {
    @PostMapping(value = "/internal/ai/interest-summary")
    String summarizeInterest(@RequestBody InterestSummaryRequest request);
}
