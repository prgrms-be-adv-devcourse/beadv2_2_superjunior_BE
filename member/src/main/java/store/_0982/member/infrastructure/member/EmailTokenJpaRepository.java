package store._0982.member.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.member.domain.member.EmailToken;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmailTokenJpaRepository extends JpaRepository<EmailToken, UUID> {
    Optional<EmailToken> findByEmail(String email);
    @Modifying
    @Query("delete from EmailToken et where et.expiredAt < :now")
    void deleteExpiredTokens(@Param("now") OffsetDateTime now);

}
