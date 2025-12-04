package store._0982.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "address", schema = "member_schema")
public class Address {

    @Id
    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Column(name = "address", length = 100, nullable = false)
    private String address;

    @Column(name = "address_detail", length = 100, nullable = false)
    private String addressDetail;

    @Column(name = "postal_code", length = 50, nullable = false)
    private String postalCode;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

