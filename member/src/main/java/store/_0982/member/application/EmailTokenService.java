package store._0982.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.log.ServiceLog;
import store._0982.member.domain.EmailTokenRepository;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailTokenService {
    private final EmailTokenRepository emailTokenRepository;

    @ServiceLog
    public void cleanUpExpiredEmailTokens() {
        emailTokenRepository.deleteExpiredEmailTokens(OffsetDateTime.now());
    }
}
