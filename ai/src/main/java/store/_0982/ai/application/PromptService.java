package store._0982.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;
import store._0982.ai.application.dto.SimpleGroupPurchaseInfo;
import store._0982.ai.application.dto.LlmResponse;
import store._0982.common.log.ServiceLog;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final ObjectMapper objectMapper;
    private final ChatModel chatModel;

    @ServiceLog
    public LlmResponse askToChatModel(String keyword, String category, List<SimpleGroupPurchaseInfo> gpInfos, int numOfReco){
        try {
            Prompt prompt = generatePrompt(keyword, category, gpInfos, numOfReco);
            return parseResponse(chatModel.call(prompt));
        } catch (JsonProcessingException e) {
            return new LlmResponse(List.of(), "");
        }
    }

    private Prompt generatePrompt(String keyword, String category , List<SimpleGroupPurchaseInfo> gpInfos, int numOfReco) throws JsonProcessingException {
        String gpInfosJson = objectMapper.writeValueAsString(gpInfos);

        PromptTemplate systemTemplate = new SystemPromptTemplate(

                """
                        너는 공동구매 추천 도우미야.
                        
                        규칙:
                        - 키워드와 카테고리 적합도가 높은 순으로 최대 {n}개 추천
                        - 동일 상품 중복 제거
                        - 아래 형태의 JSON 배열로만 응답 
                            {
                               "groupPurchases": [
                                    {
                                        "groupPurchaseId": "3f9c2e0a-1b7d-4e45-8c8a-2f9a0f2b9c41",
                                        "rank": 1
                                    },
                                    {
                                        "groupPurchaseId": "a7b1d6c9-5c42-4f3a-9e64-8d0b1e6a73f2",
                                        "rank": 2
                                    },
                                    {
                                        "groupPurchaseId": "e4c8a9f1-2d6e-4a7b-bf31-9c0d8a1e5b24",
                                        "rank": 3
                                    }
                               ],
                               "reason": "추천 이유"
                            }
                        - groupPurchaseIds는 추천순으로 정렬
                        """
        );
        systemTemplate.add("n", numOfReco);

        PromptTemplate userTemplate = new PromptTemplate(
                """
                        사용자 검색어: {keyword}
                        선호 카테고리: {category}
                        후보 목록(JSON): {gpInfos}
                        
                        상품을 추천하고,
                        추천 이유를 각 항목당 100자 이내로 작성해줘.
                        """
        );
        userTemplate.add("keyword", keyword);
        userTemplate.add("category", category);
        userTemplate.add("gpInfos", gpInfosJson);

        return new Prompt(List.of(
                systemTemplate.createMessage(),
                userTemplate.createMessage()
        ));
    }

    private LlmResponse parseResponse(ChatResponse response) throws JsonProcessingException {
        String content = response.getResult().getOutput().getContent();
        return objectMapper.readValue(content, LlmResponse.class);
    }


    public String summarizeInterest(List<String> descriptions){
        Prompt prompt = generateInterestSummaryPrompt(descriptions);
        return chatModel.call(prompt).getResult().getOutput().getContent();
    }
    private Prompt generateInterestSummaryPrompt(List<String> descriptions) {
        PromptTemplate systemTemplate = new SystemPromptTemplate(

                """
                    너는 사용자가 구매한 상품들의 설명들을 받고, 그를 기반으로 사용자의 관심을 요약해주는 기계야.
                    
                    규칙:
                    - 400자 이내의 영어로 답변 
                    - 답변 형식: //{"summarize": "I have a strong interest in products related to companion animals."//}
                """

        );
        PromptTemplate userTemplate = new PromptTemplate(
                """
                        사용자가 구매한 상품들의 설명 목록: {descriptions}
                        내 관심을 요약해줘.
                """
        );
        userTemplate.add("descriptions", descriptions.toString());

        return new Prompt(List.of(
                systemTemplate.createMessage(),
                userTemplate.createMessage()
        ));
    }
}
