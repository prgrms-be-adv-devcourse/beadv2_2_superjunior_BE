package store._0982.member.domain.member;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface EmailTokenRepository {

    EmailToken save(EmailToken emailToken);

    Optional<EmailToken> findByEmail(String email);

    void deleteExpiredEmailTokens(OffsetDateTime now);
}
