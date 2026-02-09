package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.exception.CustomException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupPurchaseQuantityService {

    private final GroupPurchaseRepository groupPurchaseRepository;

    public GroupPurchase increaseQuantity(UUID groupPurchaseId, int quantity) {
        int updated = groupPurchaseRepository.increaseQuantity(groupPurchaseId, quantity);

        if(updated == 0){
            throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_REACHED);
        }

        return groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));
    }

    @Transactional
    public void decreaseQuantity(UUID groupPurchaseId, int quantity) {
        int updated = groupPurchaseRepository.decreaseQuantity(groupPurchaseId, quantity);
        if(updated == 0){
            throw new CustomException(CustomErrorCode.DECREASE_QUANTITY_FAILED);
        }
    }

}
