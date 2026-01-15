package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.constant.BonusEarningType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "bonus_earning", schema = "payment_schema")
public class BonusEarning {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "amount", nullable = false)
    private long amount;                    // 적립된 보너스 포인트

    @Column(name = "remaining_amount", nullable = false)
    private long remainingAmount;           // 남은 보너스 포인트 (사용/만료로 감소)

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BonusEarningType type;

    @Column(name = "policy_id")
    private UUID policyId;                  // 어떤 정책으로 적립되었는지

    @Column(name = "order_id")
    private UUID orderId;                   // 구매 적립인 경우 주문 ID

    @Column(name = "reference_id")
    private UUID referenceId;               // 이벤트 ID, 추천인 ID 등

    @Column(name = "description")
    private String description;             // "2025년 1월 구매 적립", "신규 가입 축하"

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;       // 만료 시각

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BonusEarningStatus status;

    @CreationTimestamp
    @Column(name = "earned_at", nullable = false)
    private OffsetDateTime earnedAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;          // 완전히 사용된 시각

    @Column(name = "expired_at")
    private OffsetDateTime expiredAt;       // 실제 만료된 시각
}
