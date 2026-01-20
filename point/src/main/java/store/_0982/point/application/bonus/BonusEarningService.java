package store._0982.point.application.bonus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.bonus.BonusEarnCommand;
import store._0982.point.common.RetryableTransactional;
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

    @RetryableTransactional
    public void processBonus(UUID memberId, BonusEarnCommand command) {
        PointTransaction transaction = getValidTransaction(memberId, command.orderId());
        PointBalance balance = pointBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        findApplicablePolicy(transaction.getPaidAmount(), command)
                .ifPresent(policy -> applyBonusPolicy(memberId, command, balance, transaction, policy));
    }

    private PointTransaction getValidTransaction(UUID memberId, UUID orderId) {
        PointTransaction transaction = pointTransactionRepository
                .findByOrderIdAndStatus(orderId, PointTransactionStatus.USED)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        transaction.validateOwner(memberId);
        return transaction;
    }

    private Optional<BonusPolicy> findApplicablePolicy(long paidAmount, BonusEarnCommand command) {
        return bonusPolicyRepository.findBestPolicy(
                BonusPolicyType.PURCHASE_REWARD,
                paidAmount,
                command.groupPurchaseId(),
                command.productCategory()
        );
    }

    private void applyBonusPolicy(UUID memberId, BonusEarnCommand command,
                                  PointBalance balance, PointTransaction transaction,
                                  BonusPolicy policy) {
        policy.calculateBonusAmount(transaction.getPaidAmount())
                .ifPresentOrElse(
                        bonusAmount -> earnBonus(memberId, command, balance, policy, bonusAmount),
                        () -> log.debug("보너스 금액 계산 실패")
                );
    }

    private void earnBonus(UUID memberId, BonusEarnCommand command,
                           PointBalance balance, BonusPolicy policy, Long bonusAmount) {
        PointTransaction earningTx = PointTransaction.bonusEarned(
                memberId, command.orderId(), command.idempotencyKey(), PointAmount.bonus(bonusAmount));

        BonusEarning bonusEarning = BonusEarning.fromOrder(
                memberId,
                bonusAmount,
                command.orderId(),
                OffsetDateTime.now().plusDays(policy.getExpirationDays()).withHour(23).withMinute(59).withSecond(59),
                policy.getId(),
                policy.getName()
        );

        pointTransactionRepository.saveAndFlush(earningTx);
        bonusEarningRepository.save(bonusEarning);
        balance.earnBonus(bonusAmount);
        applicationEventPublisher.publishEvent(BonusEarnedTxEvent.from(bonusEarning));
    }
}
