package store._0982.member.infrastructure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.member.EmailToken;
import store._0982.member.domain.member.EmailTokenRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailTokenRepositoryAdapter implements EmailTokenRepository {
    private final EmailTokenJpaRepository emailTokenJpaRepository;

    @Override
    public EmailToken save(EmailToken emailToken) {
        return emailTokenJpaRepository.save(emailToken);
    }

    @Override
    public Optional<EmailToken> findByEmail(String email) {
        return emailTokenJpaRepository.findByEmail(email);
    }

    @Override
    public void deleteExpiredEmailTokens(OffsetDateTime now) {
        emailTokenJpaRepository.deleteExpiredTokens(now);
    }

}
