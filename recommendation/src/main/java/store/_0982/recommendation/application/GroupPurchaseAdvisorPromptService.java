package store._0982.recommendation.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;
import store._0982.recommendation.application.dto.PriceQuantityAdvice;
import store._0982.recommendation.infrastructure.feign.commerce.dto.GroupPurchasePerformanceInfo;
import store._0982.recommendation.infrastructure.feign.commerce.dto.ProductDetailInfo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupPurchaseAdvisorPromptService {
    private final ObjectMapper objectMapper;
    private final ChatModel chatModel;

    public PriceQuantityAdvice generatePriceQuantityAdvice(
            ProductDetailInfo product,
            List<GroupPurchasePerformanceInfo> performances
    ){
        try{
            Prompt prompt = generatePrompt(product, performances);
            return parseResponse(chatModel.call(prompt), PriceQuantityAdvice.class);
        }catch (JsonProcessingException e){
            return new PriceQuantityAdvice(
                    0L, 0, 0, 0.0, 0, "parsing failed", 0.0, 0, List.of()
            );
        }
    }

    private Prompt generatePrompt(
            ProductDetailInfo product,
            List<GroupPurchasePerformanceInfo> performances
    ) throws JsonProcessingException{
        String productJson = objectMapper.writeValueAsString(product);
        String performanceJson = objectMapper.writeValueAsString(performances);

        PromptTemplate systemTemplate = new SystemPromptTemplate(
                """
                너는 공동구매 가격/수량 전문 어드바이저야.
                과거 유사 공동구매 데이터를 분석해 최적의 가격과 수량을 추천해줘.
                
                규칙:
                - 반드시 JSON으로만 응답
                - 아래 스키마를 반드시 지켜줘
                {{
                    "recommendedDiscountedPrice": 0,
                    "recommendedMinQuantity": 0,
                    "recommendedMaxQuantity": 0,
                    "recommendedDiscountRate": 0,
                    "recommendedDurationDays": 0,
                    "reason": "",
                    "confidence" : 0.0,
                    "analyzedCases": 0,
                    "similarCases": [
                        {{
                            "title": "",
                            "discountRate": 0.0,
                            "participationRate": 0.0,
                            "status": ""
                        }}
                    ]
                }}
                - 숫자는 현실적인 범위로 추천
                - reason은 100자 이내
                """
        );

        PromptTemplate userTemplate = new PromptTemplate(
                """
                현재 상품 정보:
                {productJson}
                
                과거 유사 공동구매 성과 데이터(JSON 배열):
                {performanceJson}
                
                요청:
                위 데이터를 분석해서 최적의 할인 가격, 최소/최대 수량, 기간을 추천해줘.
                """
        );

        userTemplate.add("productJson",productJson);
        userTemplate.add("performanceJson", performanceJson);

        return new Prompt(List.of(
                systemTemplate.createMessage(),
                userTemplate.createMessage()
        ));
    }

    private <T> T parseResponse(ChatResponse response, Class<T> clazz) throws JsonProcessingException {
        String content = response.getResult().getOutput().getText();
        return objectMapper.readValue(content, clazz);
    }
}
