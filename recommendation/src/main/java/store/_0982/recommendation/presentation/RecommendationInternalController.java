package store._0982.recommendation.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import store._0982.recommendation.application.PromptService;
import store._0982.recommendation.presentation.dto.InterestSummaryRequest;

@RestController
@RequiredArgsConstructor
public class RecommendationInternalController {

    private final PromptService promptService;

    @PostMapping("/internal/recommendations/interest-summary")
    public String summarizeInterest(@RequestBody InterestSummaryRequest request){
        return promptService.summarizeInterest(request.descriptions());
    }

}
