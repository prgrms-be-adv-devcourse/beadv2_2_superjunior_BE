package store._0982.ai.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.test.util.ReflectionTestUtils;
import store._0982.ai.application.dto.LlmResponse;
import store._0982.ai.application.dto.SimpleGroupPurchaseInfo;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock
    private ChatModel chatModel;

    private PromptService promptService;

    @BeforeEach
    void setUp() {
        promptService = new PromptService(new ObjectMapper(), chatModel);
    }

    @Test
    void parseResponse_shouldReturnParsedLlmResponse() {
        String json = """
                {
                  "groupPurchases":[
                    {"groupPurchaseId":"00000000-0000-0000-0000-000000000001","rank":2},
                    {"groupPurchaseId":"00000000-0000-0000-0000-000000000000","rank":1}
                  ],
                  "reason":"top picks"
                }
                """;
        ChatResponse response = new ChatResponse(
                List.of(new Generation(new AssistantMessage(json)))
        );

        LlmResponse result = ReflectionTestUtils.invokeMethod(promptService, "parseResponse", response);

        assertThat(result.reason()).isEqualTo("top picks");
        assertThat(result.groupPurchases())
                .extracting(LlmResponse.GroupPurchase::groupPurchaseId)
                .containsExactly(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        UUID.fromString("00000000-0000-0000-0000-000000000000")
                );
    }

    @Test
    void askToChatModel_shouldCallModelAndReturnParsedResponse() {
        String json = """
                {
                  "groupPurchases":[{"groupPurchaseId":"00000000-0000-0000-0000-000000000000","rank":1}],
                  "reason":"fits query"
                }
                """;
        ChatResponse response = new ChatResponse(
                List.of(new Generation(new AssistantMessage(json)))
        );
        when(chatModel.call(any(Prompt.class))).thenReturn(response);

        LlmResponse result = promptService.askToChatModel(
                "키워드", "카테고리",
                List.of(new SimpleGroupPurchaseInfo("00000000-0000-0000-0000-000000000000", "t", "d", "p", "c")),
                3
        );

        verify(chatModel).call(any(Prompt.class));
        assertThat(result.reason()).isEqualTo("fits query");
        assertThat(result.groupPurchases())
                .hasSize(1)
                .first()
                .satisfies(gp -> {
                    assertThat(gp.groupPurchaseId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000000"));
                    assertThat(gp.rank()).isEqualTo(1);
                });
    }
}
