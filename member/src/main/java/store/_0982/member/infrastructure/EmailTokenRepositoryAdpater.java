package store._0982.member.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.EmailToken;
import store._0982.member.domain.EmailTokenRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailTokenRepositoryAdpater implements EmailTokenRepository {
    private final EmailTokenJpaRepository emailTokenJpaRepository;

    @Override
    public EmailToken save(EmailToken emailToken) {
        return emailTokenJpaRepository.save(emailToken);
    }

    @Override
    public Optional<EmailToken> findByToken(String token) {
        return emailTokenJpaRepository.findByToken(token);
    }

    @Override
    public Optional<EmailToken> findByEmail(String email) {
        return emailTokenJpaRepository.findByEmail(email);
    }

    @Override
    public void deleteByEmail(String email) {
        emailTokenJpaRepository.deleteByEmail(email);
    }

}
