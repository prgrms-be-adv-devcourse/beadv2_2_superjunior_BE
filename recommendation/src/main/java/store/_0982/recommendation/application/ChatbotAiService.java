package store._0982.recommendation.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Service;
import store._0982.recommendation.application.dto.ChatbotParseResult;
import store._0982.recommendation.application.dto.ChatbotReplyResult;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotAiService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public ChatbotParseResult parseUserMessage(String message) {
        String system = """
            너는 쇼핑 추천 챗봇 파서다.
            사용자 입력에서 intent/category/priceRange/preferences 를 추출해.
            반드시 parse_user_message tool을 호출해 결과를 반환해라.
            intent는 RECOMMEND 또는 OTHER 중 하나.
            category는 HOME, FOOD, HEALTH, BEAUTY, FASHION, ELECTRONICS, KIDS, HOBBY, PET 또는 빈 문자열.
            priceRange는 원문 그대로 또는 빈 문자열.
            preferences는 핵심 키워드만 배열로.
            """;

        ChatResponse response = chatClient.prompt()
                .system(system)
                .user(message)
                .tools("parse_user_message")
                .call()
                .chatResponse();

        return extractToolResult(response, ChatbotParseResult.class);
    }

    public ChatbotReplyResult composeReply(String userMessage, String context, String nextQuestion) {
        String system = """
            너는 친근한 쇼핑 추천 챗봇이야.
            사용자 감정/의도를 한 문장으로 공감하고, 자연스럽게 다음 행동을 제안해.
            context와 nextQuestion을 참고해 1~2문장으로 답변을 작성한다.
            반드시 compose_chatbot_reply tool을 호출해 결과를 반환해라.
            """;

        String user = """
            user_message: %s
            context: %s
            nextQuestion: %s
            """.formatted(userMessage, context, nextQuestion);

        ChatResponse response = chatClient.prompt()
                .system(system)
                .user(user)
                .tools("compose_chatbot_reply")
                .call()
                .chatResponse();

        return extractToolResult(response, ChatbotReplyResult.class);
    }

    private <T> T extractToolResult(ChatResponse response, Class<T> clazz) {
        try {
            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                return null;
            }
            Generation generation = response.getResult();
            if (generation == null || generation.getOutput() == null) {
                return null;
            }

            Object toolResult = generation.getOutput().getMetadata().get("tool_result");
            if (toolResult == null) {
                toolResult = generation.getOutput().getMetadata().get("toolResult");
            }

            if (toolResult instanceof Map<?, ?> map) {
                return objectMapper.convertValue(map, clazz);
            }

            String content = generation.getOutput().getText();
            if (content != null && content.trim().startsWith("{")) {
                return objectMapper.readValue(content, clazz);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
