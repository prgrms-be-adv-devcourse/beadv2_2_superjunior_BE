package store._0982.recommendation.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.recommendation.application.RecommendationService;
import store._0982.recommendation.application.dto.RecommandInfo;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    @ControllerLog
    public ResponseDto<RecommandInfo> getRecommendations(
            @RequestHeader(value = HeaderName.ID) UUID memberId
    ) {
        return new ResponseDto<>(HttpStatus.OK, recommendationService.getRecommendations(memberId), "추천 공동구매 목록");
    }
}
