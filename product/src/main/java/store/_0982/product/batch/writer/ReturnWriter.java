package store._0982.product.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseRepository;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReturnWriter implements ItemWriter<GroupPurchase> {
    private final GroupPurchaseRepository groupPurchaseRepository;
    @Override
    public void write(Chunk<? extends GroupPurchase> chunk) throws Exception {
        List<? extends GroupPurchase> items = chunk.getItems();

        List<GroupPurchase> successItems = items.stream()
                .filter(Objects::nonNull)
                .map(gp -> (GroupPurchase) gp)
                .toList();

        long successCount = 0;
        long failCount = 0;

        for (GroupPurchase gp : successItems) {
            try {
                gp.markAsReturned();
                groupPurchaseRepository.save(gp);
                successCount++;
            } catch (IllegalStateException e) {
                failCount++;
                log.warn("환불 처리 실패: groupPurchaseId={}, {}", gp.getGroupPurchaseId(), e.getMessage());
                // TODO: 관리자 알림 처리?
            }
        }

        log.info("환불 처리 완료: 성공 {}, 실패 {}", successCount, failCount);
    }
}
