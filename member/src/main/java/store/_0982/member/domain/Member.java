package store._0982.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "member", schema = "member_schema")
public class Member {

    @Id
    @Column(name = "member_id", nullable = false)
    private UUID id;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role = Role.CUSTOMER;

    @Column(name = "salt_key", length = 32, nullable = false)
    private String saltKey;

    @Column(name = "point_balance", nullable = false)
    private Integer pointBalance = 0;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Member create(String email, String name, String password, String phoneNumber){
        Member member = new Member();
        member.email = email;
        member.name = name;
        member.password = password;
        member.phoneNumber = phoneNumber;
        member.saltKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        member.createdAt = LocalDateTime.now();
        return member;
    }

    public void changePassword(String password) {
        this.password = password;
    }
}

