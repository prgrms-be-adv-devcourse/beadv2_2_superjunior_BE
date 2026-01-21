package store._0982.batch.batch.grouppurchase.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultWithProductInfo;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.product.Product;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateStatusClosedGroupPurchaseProcessor implements ItemProcessor<GroupPurchase, GroupPurchaseResultWithProductInfo> {

    private Map<UUID, Product> productMap;

    public void setProductMap(Map<UUID, Product> productMap) {
        this.productMap = productMap;
    }

    @Nullable
    @Override
    public GroupPurchaseResultWithProductInfo process(GroupPurchase groupPurchase) throws Exception {

        boolean isSuccess = groupPurchase.getCurrentQuantity() >= groupPurchase.getMinQuantity();

        Product product = productMap.get(groupPurchase.getProductId());

        if(product == null){
            throw new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND);
        }

        return new GroupPurchaseResultWithProductInfo(groupPurchase, product, isSuccess);
    }
}
