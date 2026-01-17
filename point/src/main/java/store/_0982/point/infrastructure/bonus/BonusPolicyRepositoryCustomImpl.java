package store._0982.point.infrastructure.bonus;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.constant.BonusPolicyType;
import store._0982.point.domain.entity.BonusPolicy;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static store._0982.point.domain.entity.QBonusPolicy.bonusPolicy;

@Repository
@RequiredArgsConstructor
public class BonusPolicyRepositoryCustomImpl implements BonusPolicyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // TODO: 적합한 보너스 정책을 찾는 기능을 좀 더 구체화하자
    @Override
    public Optional<BonusPolicy> findBestPolicy(
            BonusPolicyType type,
            Long purchaseAmount,
            UUID groupPurchaseId,
            String category
    ) {
        OffsetDateTime now = OffsetDateTime.now();

        return Optional.ofNullable(queryFactory
                .selectFrom(bonusPolicy)
                .where(
                        bonusPolicy.isActive.isTrue(),
                        bonusPolicy.type.eq(type),
                        isValidPeriod(now),
                        goeMinPurchaseAmount(purchaseAmount),
                        matchesTarget(groupPurchaseId, category)
                )
                // 혜택이 큰 순서: 적립률 > 고정금액 > 최신순
                .orderBy(
                        bonusPolicy.rewardRate.desc().nullsLast(),
                        bonusPolicy.fixedAmount.desc().nullsLast(),
                        bonusPolicy.id.desc()
                )
                .fetchFirst());
    }

    private BooleanExpression isValidPeriod(OffsetDateTime now) {
        return (bonusPolicy.validFrom.isNull().or(bonusPolicy.validFrom.loe(now)))
                .and(bonusPolicy.validUntil.isNull().or(bonusPolicy.validUntil.after(now)));
    }

    private BooleanExpression goeMinPurchaseAmount(Long purchaseAmount) {
        if (purchaseAmount == null) {
            return null;
        }
        return bonusPolicy.minPurchaseAmount.isNull()
                .or(bonusPolicy.minPurchaseAmount.loe(purchaseAmount));
    }

    private BooleanExpression matchesTarget(UUID groupPurchaseId, String category) {
        // 공구 ID: 정책이 특정 공구 ID를 지정하지 않았거나(전체), 내 공구 ID와 일치
        BooleanExpression groupCondition = bonusPolicy.targetGroupPurchaseId.isNull();
        if (groupPurchaseId != null) {
            groupCondition = groupCondition.or(bonusPolicy.targetGroupPurchaseId.eq(groupPurchaseId));
        }

        // 카테고리: 정책이 특정 카테고리를 지정하지 않았거나(전체), 내 카테고리와 일치
        BooleanExpression categoryCondition = bonusPolicy.targetCategory.isNull();
        if (category != null) {
            categoryCondition = categoryCondition.or(bonusPolicy.targetCategory.eq(category));
        }

        return groupCondition.and(categoryCondition);
    }
}
