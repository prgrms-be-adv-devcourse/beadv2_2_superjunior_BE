package store._0982.batch.batch.elasticsearch.processor;

import org.springframework.batch.item.ItemProcessor;
import store._0982.batch.domain.elasticsearch.GroupPurchaseReindexRow;
import store._0982.batch.domain.elasticsearch.GroupPurchaseDocument;

public class GroupPurchaseReindexProcessor implements ItemProcessor<GroupPurchaseReindexRow, GroupPurchaseDocument> {
    @Override
    public GroupPurchaseDocument process(GroupPurchaseReindexRow item) {
        return GroupPurchaseDocument.fromReindexRow(item);
    }
}
