package store._0982.member.domain;

import java.util.Optional;

public interface EmailTokenRepository {

    EmailToken save(EmailToken emailToken);

    Optional<EmailToken> findByToken(String token);

    Optional<EmailToken> findByEmail(String email);

    void deleteByEmail(String email);
}
