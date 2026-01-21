package store._0982.batch.batch.grouppurchase.processor;

import lombok.NonNull;
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
public class OpenGroupPurchaseProcessor implements ItemProcessor<GroupPurchase, GroupPurchaseResultWithProductInfo> {

    private Map<UUID, Product> productMap;

    public void setProductMap(Map<UUID, Product> productMap){
        this.productMap = productMap;
    }
    @Nullable
    @Override
    public GroupPurchaseResultWithProductInfo process(@NonNull GroupPurchase groupPurchase) throws Exception {
        // 공동구매 오픈 처리
        groupPurchase.open();

        Product product = productMap.get(groupPurchase.getProductId());

        if(product == null) {
            throw new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND);
        }

        return new GroupPurchaseResultWithProductInfo(groupPurchase, product, true);
    }
}
