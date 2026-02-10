package store._0982.elasticsearch.infrastructure.client.commerce;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRow;
import store._0982.elasticsearch.infrastructure.client.commerce.dto.GroupPurchaseIdsRequest;

@FeignClient(
        name = "commerce-service",
        url = "${client.commerce}"
)
public interface CommerceSearchClient {

    @PostMapping("/internal/purchases/search")
    List<GroupPurchaseSearchRow> findByIds(@RequestBody GroupPurchaseIdsRequest request);
}
