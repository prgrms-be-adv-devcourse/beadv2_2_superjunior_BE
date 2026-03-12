package store._0982.recommendation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.domain.product.ProductCategory;
import store._0982.recommendation.application.dto.ChatbotParseResult;
import store._0982.recommendation.application.dto.ChatbotReplyResult;
import store._0982.recommendation.application.dto.VectorSearchRequest;
import store._0982.recommendation.application.dto.VectorSearchResponse;
import store._0982.recommendation.infrastructure.feign.commerce.CommerceFeignClient;
import store._0982.recommendation.infrastructure.feign.commerce.dto.OpenGroupPurchaseInfo;
import store._0982.recommendation.infrastructure.feign.commerce.dto.ProductDetailInfo;
import store._0982.recommendation.presentation.dto.ChatbotCriteria;
import store._0982.recommendation.presentation.dto.ChatbotRecommendRequest;
import store._0982.recommendation.presentation.dto.ChatbotRecommendResponse;
import store._0982.recommendation.presentation.dto.ChatbotRecommendationCard;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotRecommendationService {
    private static final int DEFAULT_LIMIT = 200;
    private static final int TOK_K = 50;
    private static final int RESULT_LIMIT = 5;
    private static final int FALLBACK_LIMIT = 200;
    
    private static final Set<String> STOPWORDS = Set.of(
            "상품", "추천", "추천해줘", "추천해", "찾아", "찾아줘", "원해", "원해요",
            "먹고", "싶어", "싶어요", "줘", "해주세요", "있어", "주세요"
    );

    private static final Set<String> GENERIC_PREFS = Set.of(
            "맛있는", "먹고싶어", "먹고", "먹고싶어요", "먹고 싶어",
            "좋은", "추천", "추천해줘", "추천해", "원해", "원해요",
            "찾아", "찾아줘", "필요", "필요해", "괜찮은", "아무거나"
    );

    private final CommerceFeignClient commerceFeignClient;
    private final EmbeddingService embeddingService;
    private final SearchQueryPort searchQueryPort;
    private final ChatbotAiService chatbotAiService;

    public ChatbotRecommendResponse recommend(ChatbotRecommendRequest request){

        OffsetDateTime now = OffsetDateTime.now();
        ChatbotParseResult parsed = chatbotAiService.parseUserMessage(request.message());
        ChatbotCriteria criteria = mergeCriteria(request,parsed);

        float[] vector = embeddingService.embedText(request.message());

        String category = criteria.category() == null ? "" : criteria.category().name();
        List<VectorSearchResponse> candidates = searchQueryPort.getRecommandationCandidates(
                new VectorSearchRequest(
                        request.message(),
                        category,
                        vector,
                        TOK_K
                )
        );

        List<ChatbotRecommendationCard> cards = (candidates == null || candidates.isEmpty())
                ? fallbackCards(criteria, now)
                : buildFromVector(candidates, criteria, now);

        if (cards.isEmpty()) {
            cards = fallbackCards(criteria, now);
        }

        List<String> suggestions = buildSuggestions(criteria, cards.size());

        String nextQuestion = suggestions.isEmpty() ? "" : suggestions.get(0);

        String context = buildReplyContext(request.message(), criteria, cards);
        ChatbotReplyResult reply = chatbotAiService.composeReply(request.message(), context, nextQuestion);
        String assistantMessage = (reply == null || reply.message() == null || reply.message().isBlank())
                ? defaultAssistantMessage(cards)
                : reply.message();
        String assistantNext = reply == null ? nextQuestion : reply.nextQuestion();

        return new ChatbotRecommendResponse(criteria, cards, suggestions, assistantMessage, assistantNext);
    }

    private ChatbotCriteria mergeCriteria(ChatbotRecommendRequest request, ChatbotParseResult parsed) {
        ProductCategory category = parseCategory(
                safeOr(parsed == null ? null : parsed.category(), request.category()),
                request.message()
        );

        String priceRange = safeOr(parsed == null ? null : parsed.priceRange(), request.priceRange());
        PriceRange range = parsePriceRange(priceRange, request.message());

        List<String> prefs = new ArrayList<>();
        prefs.addAll(parsePreferences(request.preferences()));
        if (parsed != null && parsed.preferences() != null) {
            prefs.addAll(parsed.preferences());
        }

        List<String> finalPrefs = normalizePreferences(prefs);

        return new ChatbotCriteria(category, range.min, range.max, finalPrefs);
    }

    private String safeOr(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        return b;
    }

    private List<ChatbotRecommendationCard> buildFromVector(
            List<VectorSearchResponse> candidates,
            ChatbotCriteria criteria,
            OffsetDateTime now
    ) {
        return candidates.stream()
                .map(row -> toCard(row, now))
                .filter(Objects::nonNull)
                .filter(card -> matchCategory(card, criteria))
                .filter(card -> matchPrice(card, criteria))
                .filter(card -> matchPreferences(card, criteria))
                .sorted(Comparator
                        .comparingDouble(ChatbotRecommendationCard::discountRate).reversed()
                        .thenComparingLong(ChatbotRecommendationCard::remainingMinutes))
                .limit(RESULT_LIMIT)
                .toList();
    }

    private List<ChatbotRecommendationCard> fallbackCards(ChatbotCriteria criteria, OffsetDateTime now) {
        List<OpenGroupPurchaseInfo> open = commerceFeignClient.getOpenGroupPurchases(FALLBACK_LIMIT);
        if (open == null || open.isEmpty()) return List.of();

        Map<UUID, ProductDetailInfo> productMap = fetchProducts(open);

        return open.stream()
                .map(o -> toCard(o, productMap.get(o.productId()), now))
                .filter(Objects::nonNull)
                .filter(card -> matchCategory(card, criteria))
                .filter(card -> matchPrice(card, criteria))
                .filter(card -> matchPreferences(card, criteria))
                .sorted(Comparator
                        .comparingDouble(ChatbotRecommendationCard::discountRate).reversed()
                        .thenComparingLong(ChatbotRecommendationCard::remainingMinutes))
                .limit(RESULT_LIMIT)
                .toList();
    }

    private Map<UUID, ProductDetailInfo> fetchProducts(List<OpenGroupPurchaseInfo> openGroupPurchases) {
        Set<UUID> productIds = openGroupPurchases.stream()
                .map(OpenGroupPurchaseInfo::productId)
                .collect(Collectors.toSet());

        Map<UUID, ProductDetailInfo> productMap = new HashMap<>();
        for (UUID productId : productIds) {
            ProductDetailInfo info = commerceFeignClient.getProduct(productId);
            if (info != null) productMap.put(productId, info);
        }
        return productMap;
    }

    private ChatbotRecommendationCard toCard(VectorSearchResponse row, OffsetDateTime now) {
        if (row == null || row.productSearchInfo() == null) return null;

        Long originalPrice = row.productSearchInfo().price();
        Long discountedPrice = row.discountedPrice();
        if (originalPrice == null || originalPrice <= 0 || discountedPrice == null) return null;

        OffsetDateTime endDate = parseOffsetDateTime(row.endDate());
        long remainingMinutes = endDate == null ? Long.MAX_VALUE :
                Duration.between(now, endDate).toMinutes();

        int discountRate = (int) Math.round((originalPrice - discountedPrice) * 100.0 / originalPrice);
        ProductCategory category = parseCategoryFromValue(row.productSearchInfo().category());

        return new ChatbotRecommendationCard(
                row.groupPurchaseId(),
                UUID.fromString(row.productSearchInfo().productId()),
                row.title(),
                category,
                originalPrice,
                discountedPrice,
                discountRate,
                endDate,
                remainingMinutes
        );
    }

    private ChatbotRecommendationCard toCard(OpenGroupPurchaseInfo open, ProductDetailInfo product, OffsetDateTime now) {
        if (product == null) return null;

        Long originalPrice = product.price();
        Long discountedPrice = open.discountedPrice();
        if (originalPrice == null || originalPrice <= 0 || discountedPrice == null) return null;

        int discountRate = (int) Math.round((originalPrice - discountedPrice) * 100.0 / originalPrice);
        long remainingMinutes = open.endDate() == null ? Long.MAX_VALUE :
                Duration.between(now, open.endDate()).toMinutes();

        return new ChatbotRecommendationCard(
                open.groupPurchaseId(),
                open.productId(),
                product.name(),
                product.category(),
                originalPrice,
                discountedPrice,
                discountRate,
                open.endDate(),
                remainingMinutes
        );
    }

    private boolean matchCategory(ChatbotRecommendationCard card, ChatbotCriteria criteria) {
        if (criteria.category() == null) return true;
        return criteria.category() == card.category();
    }

    private boolean matchPrice(ChatbotRecommendationCard card, ChatbotCriteria criteria) {
        Long min = criteria.minPrice();
        Long max = criteria.maxPrice();
        Long price = card.originalPrice();
        if (min != null && price < min) return false;
        if (max != null && price > max) return false;
        return true;
    }

    private boolean matchPreferences(ChatbotRecommendationCard card, ChatbotCriteria criteria) {
        List<String> prefs = criteria.preferences();
        if (prefs == null || prefs.isEmpty()) return true;

        String haystack = (card.productName() + " " + card.category().name()).toLowerCase(Locale.ROOT);
        return prefs.stream().anyMatch(p -> haystack.contains(p.toLowerCase(Locale.ROOT)));
    }


    private List<String> normalizePreferences(List<String> prefs) {
        if (prefs == null || prefs.isEmpty()) return List.of();

        return prefs.stream()
                .map(String::trim)
                .map(s -> s.replaceAll("\\s+", ""))   // 공백 제거
                .filter(s -> !s.isBlank())
                .filter(s -> !GENERIC_PREFS.contains(s))
                .distinct()
                .toList();
    }

    private String buildReplyContext(String userMessage, ChatbotCriteria criteria, List<ChatbotRecommendationCard> cards) {
        String category = criteria.category() == null ? "전체" : criteria.category().name();
        if (cards == null || cards.isEmpty()) {
            return String.format("사용자 메시지: %s | 추정 카테고리: %s | 결과 없음", userMessage, category);
        }

        ChatbotRecommendationCard top = cards.get(0);
        return String.format(
                "사용자 메시지: %s | 추정 카테고리: %s | 1순위 상품: %s | 할인율: %d%% | 남은시간: %d분",
                userMessage,
                category,
                top.productName(),
                top.discountRate(),
                top.remainingMinutes()
        );
    }

    private List<String> buildSuggestions(ChatbotCriteria criteria, int resultSize) {
        if (resultSize >= 3) return List.of();

        List<String> suggestions = new ArrayList<>();
        if (criteria.category() == null) {
            suggestions.add("어떤 카테고리에서 찾고 계세요?");
        }
        if (criteria.minPrice() == null && criteria.maxPrice() == null) {
            suggestions.add("가격대는 어느 정도가 편하세요?");
        }
        if (criteria.preferences() == null || criteria.preferences().isEmpty()) {
            suggestions.add("중요하게 보는 조건이 있나요? (예: 저자극, 브랜드)");
        }
        if (suggestions.isEmpty()) suggestions.add("조건을 조금 바꿔볼까요?");
        return suggestions;
    }

    private String defaultAssistantMessage(List<ChatbotRecommendationCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return "원하시는 내용을 더 알려주시면 추천해드릴게요.";
        }
        return "조건에 맞는 상품을 찾아봤어요.";
    }

    private ProductCategory parseCategory(String category, String message) {
        String raw = (category != null && !category.isBlank()) ? category : message;
        if (raw == null) return null;

        String upper = raw.toUpperCase(Locale.ROOT);
        for (ProductCategory pc : ProductCategory.values()) {
            if (upper.contains(pc.name())) return pc;
        }

        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.contains("먹") || lower.contains("간식") || lower.contains("식품")) return ProductCategory.FOOD;
        if (lower.contains("피부") || lower.contains("스킨") || lower.contains("화장")) return ProductCategory.BEAUTY;
        if (lower.contains("아기") || lower.contains("유아") || lower.contains("키즈")) return ProductCategory.KIDS;
        if (lower.contains("전자") || lower.contains("이어폰") || lower.contains("노트북")) return ProductCategory.ELECTRONICS;

        return null;
    }

    private ProductCategory parseCategoryFromValue(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return ProductCategory.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private PriceRange parsePriceRange(String priceRange, String message) {
        String raw = (priceRange != null && !priceRange.isBlank()) ? priceRange : message;
        if (raw == null) return PriceRange.EMPTY;

        if (raw.contains("1만원 이하")) return new PriceRange(null, 10000L);
        if (raw.contains("1~3만원")) return new PriceRange(10000L, 30000L);
        if (raw.contains("3~7만원")) return new PriceRange(30000L, 70000L);
        if (raw.contains("7만원 이상")) return new PriceRange(70000L, null);

        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isBlank()) return PriceRange.EMPTY;

        long value = Long.parseLong(digits);
        if (value <= 10000) return new PriceRange(null, 10000L);
        if (value <= 30000) return new PriceRange(10000L, 30000L);
        if (value <= 70000) return new PriceRange(30000L, 70000L);
        return new PriceRange(70000L, null);
    }

    private List<String> parsePreferences(String preferences) {
        if (preferences == null || preferences.isBlank()) return List.of();

        String raw = preferences.trim();
        if (raw.contains("없어") || raw.contains("상관없") || raw.contains("딱히")) return List.of();

        return Arrays.stream(raw.split("[,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static class PriceRange {
        private static final PriceRange EMPTY = new PriceRange(null, null);
        private final Long min;
        private final Long max;

        private PriceRange(Long min, Long max) {
            this.min = min;
            this.max = max;
        }
    }
}
