package store._0982.point.application.bonus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.bonus.BonusEarnCommand;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.common.RetryForTransactional;
import store._0982.point.domain.constant.BonusEarningType;
import store._0982.point.domain.constant.BonusPolicyType;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.BonusEarning;
import store._0982.point.domain.entity.BonusPolicy;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.BonusEarnedTxEvent;
import store._0982.point.domain.repository.BonusEarningRepository;
import store._0982.point.domain.repository.BonusPolicyRepository;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonusEarningService {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final BonusPolicyRepository bonusPolicyRepository;
    private final BonusEarningRepository bonusEarningRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @RetryForTransactional
    public void processBonus(UUID memberId, BonusEarnCommand command, OrderInfo orderInfo) {
        PointTransaction transaction = getValidTransaction(memberId, command.orderId());
        PointBalance balance = pointBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        findApplicablePolicy(transaction.getPaidAmount(), orderInfo)
                .ifPresent(policy -> applyBonusPolicy(memberId, command.idempotencyKey(), balance, transaction, policy));
    }

    private PointTransaction getValidTransaction(UUID memberId, UUID orderId) {
        PointTransaction transaction = pointTransactionRepository
                .findByOrderIdAndStatus(orderId, PointTransactionStatus.USED)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        transaction.validateOwner(memberId);
        return transaction;
    }

    private Optional<BonusPolicy> findApplicablePolicy(long paidAmount, OrderInfo orderInfo) {
        return bonusPolicyRepository.findBestPolicy(
                BonusPolicyType.PURCHASE_REWARD,
                paidAmount,
                orderInfo.groupPurchaseId(),
                null  // TODO: 나중에 상품 카테고리를 feign으로 받게 되면 추가
        );
    }

    private void applyBonusPolicy(UUID memberId, UUID idempotencyKey,
                                  PointBalance balance, PointTransaction transaction,
                                  BonusPolicy policy) {
        policy.calculateBonusAmount(transaction.getPaidAmount())
                .ifPresentOrElse(
                        bonusAmount -> earnBonus(memberId, idempotencyKey, balance, policy, bonusAmount),
                        () -> log.debug("보너스 금액 계산 실패")
                );
    }

    private void earnBonus(UUID memberId, UUID idempotencyKey,
                           PointBalance balance, BonusPolicy policy, Long bonusAmount) {
        PointTransaction earningTx = PointTransaction.charged(
                memberId, idempotencyKey, PointAmount.bonus(bonusAmount));

        BonusEarning bonusEarning = BonusEarning.earned(
                memberId,
                bonusAmount,
                BonusEarningType.PURCHASE_REWARD,
                OffsetDateTime.now().plusDays(policy.getExpirationDays()),
                policy.getId(),
                policy.getName()
        );

        pointTransactionRepository.saveAndFlush(earningTx);
        bonusEarningRepository.save(bonusEarning);
        balance.earnBonus(bonusAmount);
        applicationEventPublisher.publishEvent(BonusEarnedTxEvent.from(bonusEarning));
    }
}
