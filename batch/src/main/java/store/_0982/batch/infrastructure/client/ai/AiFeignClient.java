package store._0982.batch.infrastructure.client.ai;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.batch.infrastructure.client.ai.dto.InterestSummaryRequest;

@FeignClient(
        name = "ai-service",
        url = "${client.ai}"
)
public interface AiFeignClient {
    @PostMapping(value = "/internal/orders/consumer")
    String summarizeInterest(@RequestBody InterestSummaryRequest request);
}
