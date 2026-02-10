package store._0982.batch.batch.elasticsearch.processor;

import org.springframework.batch.item.ItemProcessor;
import store._0982.batch.domain.elasticsearch.GroupPurchaseReindexRow;

public class GroupPurchaseReindexProcessor implements ItemProcessor<GroupPurchaseReindexRow, GroupPurchaseReindexRow> {
    @Override
    public GroupPurchaseReindexRow process(GroupPurchaseReindexRow item) {
        return item;
    }
}
