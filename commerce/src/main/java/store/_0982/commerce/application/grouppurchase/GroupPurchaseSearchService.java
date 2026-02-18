package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseSearchRow;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupPurchaseSearchService {

    private final GroupPurchaseRepository groupPurchaseRepository;
    public List<GroupPurchaseSearchRow> findSearchRowsByIds(List<UUID> purchaseIds) {
        if (purchaseIds == null || purchaseIds.isEmpty()) {
            return List.of();
        }
        List<GroupPurchaseSearchRow> rows = groupPurchaseRepository.findSearchRowsByIds(purchaseIds);
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<UUID, GroupPurchaseSearchRow> rowMap = rows.stream()
                .collect(Collectors.toMap(GroupPurchaseSearchRow::groupPurchaseId, Function.identity()));

        List<GroupPurchaseSearchRow> ordered = new ArrayList<>(purchaseIds.size());
        for (UUID purchaseId : purchaseIds) {
            GroupPurchaseSearchRow row = rowMap.get(purchaseId);
            if (row != null) {
                ordered.add(row);
            }
        }
        return ordered;
    }
}
