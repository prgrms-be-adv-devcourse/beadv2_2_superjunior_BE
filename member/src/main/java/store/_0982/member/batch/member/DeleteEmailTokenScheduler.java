package store._0982.member.batch.member;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store._0982.member.application.member.EmailTokenService;

@Component
@RequiredArgsConstructor
public class DeleteEmailTokenScheduler {

    private final EmailTokenService emailTokenService;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        emailTokenService.cleanUpExpiredEmailTokens();
    }
}
