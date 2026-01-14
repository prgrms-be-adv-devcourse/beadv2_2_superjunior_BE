package store._0982.batch.batch.elasticsearch.reindex.processor;

import org.springframework.batch.item.ItemProcessor;
import store._0982.batch.batch.elasticsearch.reindex.domain.GroupPurchaseReindexRow;
import store._0982.batch.batch.elasticsearch.reindex.document.GroupPurchaseDocument;

public class GroupPurchaseReindexProcessor implements ItemProcessor<GroupPurchaseReindexRow, GroupPurchaseDocument> {
    @Override
    public GroupPurchaseDocument process(GroupPurchaseReindexRow item) {
        return GroupPurchaseDocument.fromReindexRow(item);
    }
}
