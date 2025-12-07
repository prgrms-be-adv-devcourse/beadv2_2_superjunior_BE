package store._0982.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "settlement_balance", nullable = false)
    private int settlementBalance = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "account_number", length = 20, nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "bank_code", length = 20, nullable = false)
    private String bankCode;

    @Column(name = "account_holder", length = 50, nullable = false)
    private String accountHolder;

    @Column(name = "business_registration_number", length = 15, nullable = false, unique = true)
    private String businessRegistrationNumber;

    public static Seller create(settlementBalance) {


    }
}

