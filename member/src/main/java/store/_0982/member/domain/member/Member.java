package store._0982.member.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store._0982.common.auth.Role;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "member", schema = "member_schema")
public class Member {

    @Id
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role = Role.CONSUMER;

    @Column(name = "salt_key", length = 32, nullable = false)
    private String saltKey;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();

    @Column(name = "status")
    private Status status = Status.PENDING;

    public static Member create(String email, String name, String password, String phoneNumber) {
        Member member = new Member();
        member.memberId = UUID.randomUUID();
        member.email = email;
        member.name = name;
        member.password = password;
        member.phoneNumber = phoneNumber;
        member.saltKey = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        member.createdAt = OffsetDateTime.now();
        return member;
    }

    public static Member createGuest() {
        Member member = new Member();
        member.role = Role.GUEST;
        return member;
    }

    public void changePassword(String password) {
        this.password = password;
        this.updatedAt = OffsetDateTime.now();
    }

    public void encodePassword(String password) {
        this.password = password;
    }

    public void update(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.updatedAt = OffsetDateTime.now();
    }

    public void delete() {
        this.deletedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void registerSeller() {
        this.role = Role.SELLER;
        this.updatedAt = OffsetDateTime.now();
    }

    public void confirm() {
        this.status = Status.ACTIVE;
    }

    public enum Status{
        PENDING, ACTIVE
    }
}
