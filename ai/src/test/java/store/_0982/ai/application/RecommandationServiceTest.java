package store._0982.ai.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import store._0982.ai.application.dto.LlmResponse;
import store._0982.ai.application.dto.GroupPurchase;
import store._0982.ai.domain.PersonalVectorRepository;
import store._0982.ai.infrastructure.feign.search.dto.ProductSearchInfo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RecommandationServiceTest {

    @Mock
    private SearchQueryPort searchQueryPort;
    @Mock
    private PersonalVectorRepository personalVectorRepository;
    @Mock
    private PromptService promptService;

    private RecommandationService recommandationService;

    @BeforeEach
    void setUp() {
        recommandationService = new RecommandationService(searchQueryPort, personalVectorRepository, promptService);
    }

    @Test
    void convertLlmResponseToRecommandationInfo_ordersByRankAndSkipsUnknown() {
        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase first = new GroupPurchase(
                "00000000-0000-0000-0000-000000000000", 1, 10, "t1", "d1", 100L, "ACTIVE",
                "2024-01-01", "2024-01-02", now, now, 0, 10L,
                new ProductSearchInfo("p1", "cat", 10L, "url1", "seller1")
        );
        GroupPurchase second = new GroupPurchase(
                "00000000-0000-0000-0000-000000000001", 1, 10, "t2", "d2", 200L, "ACTIVE",
                "2024-01-01", "2024-01-02", now, now, 0, 20L,
                new ProductSearchInfo("p2", "cat", 20L, "url2", "seller2")
        );

        LlmResponse llmResponse = new LlmResponse(
                List.of(
                        new LlmResponse.GroupPurchase(UUID.fromString("00000000-0000-0000-0000-000000000001"), 2),
                        new LlmResponse.GroupPurchase(UUID.fromString("00000000-0000-0000-0000-000000000000"), 1),
                        new LlmResponse.GroupPurchase(UUID.fromString("00000000-0000-0000-0000-000000000999"), null)
                ),
                "reason"
        );

        List<GroupPurchase> result = ReflectionTestUtils.invokeMethod(
                recommandationService,
                "convertLlmResponseToGp",
                llmResponse,
                List.of(first, second)
        );

        assertThat(result)
                .extracting(GroupPurchase::groupPurchaseId)
                .containsExactly(
                        "00000000-0000-0000-0000-000000000000",
                        "00000000-0000-0000-0000-000000000001"
                );
    }
}
