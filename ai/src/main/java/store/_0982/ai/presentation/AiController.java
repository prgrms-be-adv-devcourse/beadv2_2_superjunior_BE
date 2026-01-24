package store._0982.ai.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.ai.application.RecommendationService;
import store._0982.ai.application.dto.RecommandInfo;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {
    private final RecommendationService recommendationService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/recommandations")
    @ControllerLog
    public ResponseDto<RecommandInfo> getRecommandations(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "category", defaultValue = "") String category
    ) {
        return new ResponseDto<>(HttpStatus.OK, recommendationService.getRecommendations(memberId, keyword, category), "추천 공동구매 목록");
    }
}
