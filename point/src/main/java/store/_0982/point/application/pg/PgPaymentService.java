package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PgCreateCommand;
import store._0982.point.application.dto.PgCreateInfo;
import store._0982.point.application.dto.PgPaymentInfo;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PgPaymentService {
    private final PgPaymentRepository pgPaymentRepository;

    @ServiceLog
    @Transactional
    public PgCreateInfo createPayment(PgCreateCommand command, UUID memberId) {
        UUID orderId = command.orderId();
        return pgPaymentRepository.findByOrderId(orderId)
                .map(PgCreateInfo::from)
                .orElseGet(() -> {
                    try {
                        PgPayment pgPayment = PgPayment.create(memberId, orderId, command.amount());
                        return PgCreateInfo.from(pgPaymentRepository.saveAndFlush(pgPayment));
                    } catch (DataIntegrityViolationException e) {
                        return pgPaymentRepository.findByOrderId(orderId)
                                .map(PgCreateInfo::from)
                                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_CREATION_FAILED));
                    }
                });

    }

    public PageResponse<PgPaymentInfo> getPaymentHistories(UUID memberId, Pageable pageable) {
        Page<PgPaymentInfo> page = pgPaymentRepository.findAllByMemberId(memberId, pageable)
                .map(PgPaymentInfo::from);
        return PageResponse.from(page);
    }

    public PgPaymentInfo getPaymentHistory(UUID id, UUID memberId) {
        PgPayment pgPayment = pgPaymentRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        pgPayment.validateOwner(memberId);
        return PgPaymentInfo.from(pgPayment);
    }
}
