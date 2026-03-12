package store._0982.recommendation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store._0982.recommendation.application.dto.ChatbotParseArgs;
import store._0982.recommendation.application.dto.ChatbotParseResult;
import store._0982.recommendation.application.dto.ChatbotReplyArgs;
import store._0982.recommendation.application.dto.ChatbotReplyResult;

import java.util.function.Function;

@Configuration
public class ChatbotToolConfig {

    @Bean(name = "parse_user_message")
    public Function<ChatbotParseArgs, ChatbotParseResult> parseUserMessage() {
        return args -> new ChatbotParseResult(
                args.intent(),
                args.category(),
                args.priceRange(),
                args.preferences()
        );
    }

    @Bean(name = "compose_chatbot_reply")
    public Function<ChatbotReplyArgs, ChatbotReplyResult> composeChatbotReply() {
        return args -> new ChatbotReplyResult(
                args.message(),
                args.nextQuestion()
        );
    }
}