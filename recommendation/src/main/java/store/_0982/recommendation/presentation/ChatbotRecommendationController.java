package store._0982.recommendation.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.recommendation.application.*;
import store._0982.recommendation.application.dto.ChatbotResponse;
import store._0982.recommendation.infrastructure.feign.search.SearchFeignClient;
import store._0982.recommendation.infrastructure.feign.search.dto.GroupPurchaseKeywordSearchRequest;
import store._0982.recommendation.infrastructure.feign.search.dto.GroupPurchaseSearchInfo;
import store._0982.recommendation.presentation.dto.ChatbotRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendation")
public class ChatbotRecommendationController {

    private final ChatbotRecommendationService chatbotRecommendationService;
    private final RecommendationService recommendationService;
    private final ChatbotService chatbotService;
    private final ChatbotIndexService chatbotIndexService;
    private final SearchFeignClient searchFeignClient;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/chatbot")
    @ControllerLog
    public ResponseDto<ChatbotResponse> chat(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @RequestBody ChatbotRequest request
    ) {
        ChatbotResponse response = chatbotService.chat(
                request.message(), memberId);
        return new ResponseDto<>(HttpStatus.OK, response, "챗봇 응답");
    }

    @PostMapping("/index-all")
    @ControllerLog
    public ResponseDto<Integer> indexAll() {
        // keyword 빈 문자열 + OPEN 상태로 전체 공동구매 조회 (벡터 불필요)
        List<GroupPurchaseSearchInfo> allGps = searchFeignClient.searchByKeyword(
                new GroupPurchaseKeywordSearchRequest("", "", List.of("OPEN"), 500)
        );

        int count = chatbotIndexService.indexAllFromSearchInfo(allGps);
        return new ResponseDto<>(HttpStatus.OK, count, "챗봇 색인 완료");
    }
}
