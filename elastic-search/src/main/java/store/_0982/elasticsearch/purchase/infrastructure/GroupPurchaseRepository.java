package store._0982.elasticsearch.purchase.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import store._0982.elasticsearch.purchase.domain.GroupPurchaseDocument;

public interface GroupPurchaseRepository extends ElasticsearchRepository<GroupPurchaseDocument, String> {
    Page<GroupPurchaseDocument>
    findByTitleContainingOrDescriptionContainingAndStatus(
            String title,
            String description,
            String status,
            Pageable pageable
    );
}
