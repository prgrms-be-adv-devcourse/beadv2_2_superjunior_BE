package store._0982.member.application.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.member.domain.member.EmailTokenRepository;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailTokenServiceTest {

    @Mock
    private EmailTokenRepository emailTokenRepository;

    @InjectMocks
    private EmailTokenService emailTokenService;

    @Test
    @DisplayName("만료된 이메일 토큰을 정리한다")
    void cleanUpExpiredEmailTokens_success() {
        // when
        emailTokenService.cleanUpExpiredEmailTokens();

        // then
        verify(emailTokenRepository).deleteExpiredEmailTokens(any(OffsetDateTime.class));
    }
}

