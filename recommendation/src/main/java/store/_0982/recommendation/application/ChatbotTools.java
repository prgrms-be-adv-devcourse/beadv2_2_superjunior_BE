package store._0982.recommendation.application;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import store._0982.recommendation.application.dto.ChatbotParseResult;
import store._0982.recommendation.application.dto.ChatbotReplyResult;

import java.util.List;

@Component
public class ChatbotTools {
    @Tool(name = "parse_user_message", description = "사용자 메시지를 추천 조건으로 파싱한다.")
    public ChatbotParseResult parseUserMessage(
            String intent,
            String category,
            String priceRange,
            List<String> preferences
    ) {
        return new ChatbotParseResult(intent, category, priceRange, preferences);
    }

    @Tool(name = "compose_chatbot_reply", description = "추천 결과를 자연스러운 한국어 응답으로 만든다.")
    public ChatbotReplyResult composeChatbotReply(
            String message,
            String nextQuestion
    ) {
        return new ChatbotReplyResult(message, nextQuestion);
    }
}
