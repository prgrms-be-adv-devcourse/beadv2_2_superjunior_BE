package store._0982.recommendation.application;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import store._0982.recommendation.application.dto.UserIntentResponse;

@Component
public class UserIntentResponseTool {

    @Tool(name = "user_intent_resonse",
        description = "유저 질문 의도 분석")
    public UserIntentResponse response(
            @ToolParam String intent,
            @ToolParam String answer
    ){
        return new UserIntentResponse(intent, answer);
    }
}
