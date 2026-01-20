package store._0982.member.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "email_token", schema = "member_schema")
public class EmailToken {

    @Id
    @Column(name = "email_token_id", nullable = false)
    private UUID emailTokenId;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "token", length = 100, nullable = false, unique = true)
    private String token;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "expired_at", nullable = false)
    private OffsetDateTime expiredAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    private static final int VALID_MINUTES = 3;

    public static EmailToken create(String email) {
        EmailToken emailToken = new EmailToken();
        emailToken.emailTokenId = UUID.randomUUID();
        emailToken.email = email;
        emailToken.token = UUID.randomUUID().toString();
        emailToken.isVerified = false;
        emailToken.createdAt = OffsetDateTime.now();
        emailToken.updatedAt = emailToken.createdAt;
        emailToken.expiredAt = emailToken.createdAt.plusMinutes(VALID_MINUTES);
        return emailToken;
    }

    public EmailToken refresh() {
        token = UUID.randomUUID().toString();
        isVerified = false;
        updatedAt = OffsetDateTime.now();
        expiredAt = updatedAt.plusMinutes(VALID_MINUTES);
        return this;
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(this.expiredAt);
    }

    public void verify() {
            this.isVerified = true;
            this.updatedAt = OffsetDateTime.now();
    }

}
