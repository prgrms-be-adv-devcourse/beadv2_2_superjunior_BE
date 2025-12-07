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
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "address", schema = "member_schema")
public class Address {

    @Id
    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "address", length = 100, nullable = false)
    private String address;

    @Column(name = "address_detail", length = 100, nullable = false)
    private String addressDetail;

    @Column(name = "postal_code", length = 5, nullable = false)
    private String postalCode;

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public static Address create(Member member, String address, String addressDetail, String postalCode, String receiverName, String phoneNumber) {
        Address addressObj = new Address();
        addressObj.member = member;
        addressObj.addressId = UUID.randomUUID();
        addressObj.address = address;
        addressObj.addressDetail = addressDetail;
        addressObj.postalCode = postalCode;
        addressObj.receiverName = receiverName;
        addressObj.phoneNumber = phoneNumber;
        addressObj.createdAt = OffsetDateTime.now();
        return addressObj;
    }

    public void update(String address, String addressDetail, String postalCode, String receiverName, String phoneNumber) {
        this.address = address;
        this.addressDetail = addressDetail;
        this.postalCode = postalCode;
        this.receiverName = receiverName;
        this.phoneNumber = phoneNumber;
    }
}


