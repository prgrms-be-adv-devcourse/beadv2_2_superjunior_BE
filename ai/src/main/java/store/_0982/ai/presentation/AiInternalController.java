package store._0982.ai.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import store._0982.ai.application.PromptService;
import store._0982.ai.presentation.dto.InterestSummaryRequest;

@RestController
@RequiredArgsConstructor
public class AiInternalController {

    private final PromptService promptService;

    @PostMapping("/internal/ai/interestSummary")
    public String summarizeInterest(@RequestBody InterestSummaryRequest request){
        return promptService.summarizeInterest(request.descriptions());
    }

}
