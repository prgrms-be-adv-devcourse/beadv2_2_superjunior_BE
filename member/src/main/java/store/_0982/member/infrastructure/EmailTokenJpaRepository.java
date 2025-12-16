package store._0982.member.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.member.domain.EmailToken;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmailTokenJpaRepository extends JpaRepository<EmailToken, UUID> {
    Optional<EmailToken> findByToken(String token);
    Optional<EmailToken> findByEmail(String email);
    void deleteByEmail(String email);
    @Modifying
    @Query("delete from EmailToken et where et.expiredAt < :now")
    void deleteExpiredTokens(@Param("now") OffsetDateTime now);

}
