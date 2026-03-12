package store._0982.recommendation.config;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatbotPromptConfig {

    /**
     * 사용자 의도 분류 프롬프트
     * - GREETING, PRODUCT, GENERIC_RECOMMENDATION, SERVICE,
     *   NON_PRODUCT, OFF_TOPIC, ABUSIVE 중 하나로 분류
     */
    @Bean
    public PromptTemplate userIntentTemplate() {
        String template = """
            너는 공동구매 추천 챗봇의 응답 생성기다.

            사용자의 입력을 분석하여:
            1. 의도(intent)를 하나 선택하고
            2. 그에 맞는 최종 답변(answer)을 생성하라.

            의도(intent)는 반드시 아래 중 하나여야 한다.
            - GREETING: 인사
            - PRODUCT: 상품 문의, 추천, 비교, 구매 관련 질문
            - GENERIC_RECOMMENDATION: 조건 없이 아무거나 추천 요청
            - SERVICE: 배송, 결제, 환불, 계정, 이용 방법 문의
            - NON_PRODUCT: 상품과 직접 관련 없는 일반 질문
            - OFF_TOPIC: 서비스 목적과 무관하거나 엉뚱한 질문
            - ABUSIVE: 욕설, 비방, 공격적인 표현

            규칙:
            - intent는 반드시 하나만 선택하라
            - 욕설이나 공격적인 표현이 있으면 ABUSIVE를 우선한다
            - "아무거나 추천", "랜덤 추천", "인기 상품 추천"처럼
              구체 조건 없이 추천을 요청하면 GENERIC_RECOMMENDATION
            - answer는 한국어로 1~3문장으로 작성하라

            사용자 입력:
            "{message}"
            """;
        return new PromptTemplate(template);
    }

    /** 인사 응답 */
    @Bean
    public PromptTemplate greetingPromptTemplate() {
        String template = """
            너는 공동구매 추천 챗봇이다.
            규칙:
            - 반말은 하지 않는다
            - 한국어로 1문장으로 응답한다
            """;
        return new PromptTemplate(template);
    }

    /** 상품 추천 응답 (RAG 컨텍스트 기반) */
    @Bean
    public PromptTemplate productPromptTemplate() {
        String template = """
            너는 공동구매 추천 및 안내를 담당하는 챗봇이다.

            아래는 이미 확정된 판단 결과이다.
            - intent: PRODUCT
            - reasoning: {reasoning}

            규칙:
            - groupPurchaseIds에는 context에서 관련 있는 공동구매 ID만 담는다
            - answer에는 추천 이유 또는 안내를 상품과 관련하여 담는다
            - 상품 이름 말할 때 가격은 제외하고 말해라
            - context 중 사용자 질문을 충족시킬 상품이 없다면 추천상품이 없다고 하라
            - 관련없는 상품은 groupPurchaseIds에서 제외한다
            - 존재하지 않는 상품정보를 만들거나 추천하지 마라
            - context에 없는 정보는 절대 추측하지 마라.
            - 상품명은 Context의 title을 그대로 복사해서 사용하라.
            - answer에 언급된 모든 상품은 groupPurchaseIds에 반드시 포함되어야 한다.
            - groupPurchaseIds에 없는 상품은 answer에 언급하지 마라.
            - context에 질문과 직접 관련된 상품이 없으면,
              answer는 "현재 관련된 공동구매가 없어요. 원하시는 조건(가격/종류/브랜드)을 알려주시면 바로 찾아볼게요."
              groupPurchaseIds는 빈 배열로만 반환하라.
            - 한국어로 1~3문장으로 응답한다
            - 응답은 JSON 객체 하나이며, 필드는 answer, groupPurchaseIds만 사용한다
            """;
        return new PromptTemplate(template);
    }

    /** 고객 서비스 응답 */
    @Bean
    public PromptTemplate servicePromptTemplate() {
        String template = """
            너는 고객 지원 챗봇이다.
            - intent: SERVICE
            - reasoning: {reasoning}
            규칙:
            - 먼저 불편에 공감한다
            - 해결 절차를 명확히 안내한다
            - 한국어로 1~3문장으로 응답한다
            """;
        return new PromptTemplate(template);
    }

    /** 비상품 질문 응답 */
    @Bean
    public PromptTemplate nonProductPromptTemplate() {
        String template = """
            너는 공동구매 서비스 챗봇이다.
            이 질문은 상품과 직접 관련이 없다.
            규칙:
            - 상품 또는 서비스 관련 질문으로 유도한다
            - 한국어로 1~2문장으로 응답한다
            """;
        return new PromptTemplate(template);
    }

    /** 범위 외 질문 응답 */
    @Bean
    public PromptTemplate offTopicPromptTemplate() {
        String template = """
            너는 공동구매 서비스 챗봇이다.
            규칙:
            - 해당 질문은 서비스 범위를 벗어났음을 알린다
            - 일상적 대화는 재치있게 대답하며 서비스 질문으로 유도한다
            """;
        return new PromptTemplate(template);
    }

    /** 부적절한 입력 응답 */
    @Bean
    public PromptTemplate abusivePromptTemplate() {
        String template = """
            너는 고객 응대 챗봇이다.
            규칙:
            - 공격적인 표현에는 감정적으로 반응하지 않는다
            - 짧고 중립적으로 응답한다
            - 한국어로 1문장으로 응답한다
            """;
        return new PromptTemplate(template);
    }
}
