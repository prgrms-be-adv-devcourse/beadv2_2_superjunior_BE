package store._0982.point.infrastructure.point;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.constant.PointType;
import store._0982.point.domain.entity.PointTransaction;

import java.util.List;
import java.util.UUID;

import static store._0982.point.domain.entity.QPointTransaction.pointTransaction;

@Repository
@RequiredArgsConstructor
public class PointTransactionRepositoryCustomImpl implements PointTransactionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PointTransaction> findAllByMemberIdAndType(UUID memberId, PointType type, Pageable pageable) {
        List<PointTransaction> content = queryFactory
                .selectFrom(pointTransaction)
                .where(
                        pointTransaction.memberId.eq(memberId),
                        eqType(type)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(pointTransaction.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(pointTransaction.count())
                .from(pointTransaction)
                .where(
                        pointTransaction.memberId.eq(memberId),
                        eqType(type)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression eqType(PointType type) {
        if (type == PointType.PAID) {
            return pointTransaction.pointAmount.paidPoint.gt(0);
        } else if (type == PointType.BONUS) {
            return pointTransaction.pointAmount.bonusPoint.gt(0);
        }
        return null;
    }
}
