package store._0982.ai.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.ai.application.RecommandationService;
import store._0982.ai.application.dto.RecommandationInfo;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {
    private final RecommandationService recommandationService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/recommandations")
    public ResponseDto<List<RecommandationInfo>> getRecommandations(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "category", defaultValue = "") String category
    ) {
        return new ResponseDto<>(HttpStatus.OK, recommandationService.getRecommandations(memberId, keyword, category), "추천 공동구매 목록");
    }
}
