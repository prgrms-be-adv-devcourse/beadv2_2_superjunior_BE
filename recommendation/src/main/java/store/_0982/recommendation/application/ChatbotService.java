package store._0982.recommendation.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import store._0982.recommendation.application.dto.ChatbotResponse;
import store._0982.recommendation.application.dto.GroupPurchase;
import store._0982.recommendation.application.dto.IntentType;
import store._0982.recommendation.application.dto.UserIntentResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {
    private final ChatClient chatClient;
    private final UserIntentResponseTool userIntentResponseTool;

    private final PromptTemplate userIntentTemplate;
    private final PromptTemplate greetingPromptTemplate;
    private final PromptTemplate productPromptTemplate;
    private final PromptTemplate servicePromptTemplate;
    private final PromptTemplate nonProductPromptTemplate;
    private final PromptTemplate offTopicPromptTemplate;
    private final PromptTemplate abusivePromptTemplate;

    // VectorStore (Spring AI pgvector)
    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

    /**
     * 챗봇 메인 로직
     */
    public ChatbotResponse chat(String message, UUID memberId){

        IntentType intent = classifyIntent(message);

        return switch(intent){
            case GREETING -> handleGreeting(message);
            case PRODUCT -> handleProduct(message, memberId);
            case GENERIC_RECOMMENDATION -> handleGenericRecommendation(memberId);
            case SERVICE -> handleService(message);
            case NON_PRODUCT -> handleNonProduct(message);
            case OFF_TOPIC -> handleOffTopic(message);
            case ABUSIVE -> handleAbusive(message);
        };
    }

    /**
     * 의도 분류
     */
    private IntentType classifyIntent(String message){
        try{
            UserIntentResponse response = chatClient
                    .prompt(userIntentTemplate.create(Map.of("message", message)))
                    .tools(userIntentResponseTool)
                    .call()
                    .entity(UserIntentResponse.class);
            log.info("chatbot intent: {}", Objects.requireNonNull(response).intent());

            return IntentType.from(response != null ? response.intent() : null);
        } catch (Exception e){
            log.warn("intent classify failed :{}", e.getMessage());
            return IntentType.OFF_TOPIC;
        }
    }

    /**
     * Product 추천
     */
    private ChatbotResponse handleProduct(String message, UUID memberId){
        try{
            // VectorStore에서 의미 기반 유사도 겁색
            List<Document> documents = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(message)
                            .topK(5)
                            .filterExpression("status == 'OPEN'")
                            .build()
            );

            if (documents == null || documents.isEmpty()) {
                return new ChatbotResponse(
                        "현재 조건에 맞는 공동구매를 찾지 못했어요. 다른 키워드로 시도해보시겠어요?",
                        List.of(), null);
            }


            // Document -> Context 문자열 반환
            String context = documents.stream()
                    .map(doc -> String.format(
                            "groupPurchaseId=%s | title=%s | description=%s | price=%s",
                            doc.getMetadata().getOrDefault("groupPurchaseId", ""),
                            doc.getMetadata().getOrDefault("title", ""),
                            doc.getMetadata().getOrDefault("description", ""),
                            doc.getMetadata().getOrDefault("discountedPrice", "")
                    ))
                    .collect(Collectors.joining("\n"));

            // LLM에 Context + 질문 전달
            String userText = """
                    질문: %s
                    Context:
                    %s
                    """.formatted(message, context);

            String content = chatClient.prompt()
                    .system(productPromptTemplate.render(Map.of(
                            "reasoning", message)))
                    .user(userText)
                    .call()
                    .content();
            log.info("content : {}",content);
            // 검색된 공동구매 목록 반환 (LLM이 선택한 ID로 필터링)
            List<GroupPurchase> recommendations = documents.stream()
                    .map(doc -> GroupPurchase.from(doc.getMetadata()))
                    .toList();

            recommendations = filterByLlmGroupPurchaseIds(content, recommendations);

            return new ChatbotResponse(content, recommendations, null);

        } catch (Exception e) {
            log.warn("product response failed: {}", e.getMessage());
            return new ChatbotResponse(
                    "죄송합니다, 상품 검색 중 오류가 발생했습니다. 다시 시도해주세요.",
                    List.of(), null);
        }
    }

    /** GENERIC_RECOMMENDATION: 인기 상품 추천 */
    private ChatbotResponse handleGenericRecommendation(UUID memberId) {
        // TODO: 기존 RecommendationService.getRecommendations() 재활용 가능
        return new ChatbotResponse(
                "어떤 종류의 상품을 찾고 계신가요? 카테고리나 가격대를 알려주시면 더 정확히 추천해드릴게요!",
                List.of(), null);
    }

    /** 인사 */
    private ChatbotResponse handleGreeting(String message) {
        String content = chatClient.prompt()
                .system(greetingPromptTemplate.render())
                .user(message)
                .call()
                .content();
        return new ChatbotResponse(content, List.of(), null);
    }

    /** 서비스 문의 */
    private ChatbotResponse handleService(String message) {
        String content = chatClient.prompt()
                .system(servicePromptTemplate.render(Map.of("reasoning", message)))
                .user(message)
                .call()
                .content();
        return new ChatbotResponse(content, List.of(), null);
    }

    /** 비상품 질문 */
    private ChatbotResponse handleNonProduct(String message) {
        String content = chatClient.prompt()
                .system(nonProductPromptTemplate.render())
                .user(message)
                .call()
                .content();
        return new ChatbotResponse(content, List.of(), null);
    }

    /** 범위 외 */
    private ChatbotResponse handleOffTopic(String message) {
        String content = chatClient.prompt()
                .system(offTopicPromptTemplate.render())
                .user(message)
                .call()
                .content();
        return new ChatbotResponse(content, List.of(), null);
    }

    /** 부적절한 입력 */
    private ChatbotResponse handleAbusive(String message) {
        String content = chatClient.prompt()
                .system(abusivePromptTemplate.render())
                .user(message)
                .call()
                .content();
        return new ChatbotResponse(content, List.of(), null);
    }

    private List<GroupPurchase> filterByLlmGroupPurchaseIds(String content, List<GroupPurchase> recommendations) {
        if (content == null || content.isBlank() || recommendations == null || recommendations.isEmpty()) {
            return recommendations;
        }
        try {
            JsonNode node = objectMapper.readTree(content);
            JsonNode idsNode = node.get("groupPurchaseIds");
            if (idsNode == null || !idsNode.isArray()) {
                return recommendations;
            }
            Set<String> ids = new HashSet<>();
            idsNode.forEach(idNode -> ids.add(idNode.asText()));
            if (ids.isEmpty()) {
                return List.of();
            }
            return recommendations.stream()
                    .filter(gp -> ids.contains(gp.groupPurchaseId().toString()))
                    .toList();
        } catch (Exception e) {
            return recommendations;
        }
    }

}
