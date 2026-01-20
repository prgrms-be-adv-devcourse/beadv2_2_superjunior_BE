package store._0982.member.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "seller", schema = "member_schema")
public class Seller {

    @Id
    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;          //member를 호출하지 않고 id에 접근하기 위해 사용

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "seller_id")
    private Member member;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "bank_code", length = 20, nullable = false)
    private String bankCode;

    @Column(name = "account_number", length = 20, nullable = false)
    private String accountNumber;

    @Column(name = "account_holder", length = 50, nullable = false)
    private String accountHolder;

    @Column(name = "business_registration_number", length = 15, nullable = false, unique = true)
    private String businessRegistrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Seller.Status status = Status.PENDING;

    public static Seller create(Member member, String bankCode, String accountNumber, String accountHolder, String businessRegistrationNumber) {
        Seller seller = new Seller();
        seller.member = member;
        seller.member.registerSeller();
        seller.createdAt = OffsetDateTime.now();
        seller.bankCode = bankCode;
        seller.accountNumber = accountNumber;
        seller.accountHolder = accountHolder;
        seller.businessRegistrationNumber = businessRegistrationNumber;
        return seller;
    }

    public void update(String bankCode, String accountNumber, String accountHolder, String businessRegistrationNumber) {
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public void confirm() {
        this.status = Status.ACTIVE;
    }


    private enum Status {
        PENDING, ACTIVE
    }
}
