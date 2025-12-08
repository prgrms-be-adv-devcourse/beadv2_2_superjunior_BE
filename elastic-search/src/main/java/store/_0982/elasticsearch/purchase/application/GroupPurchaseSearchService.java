package store._0982.elasticsearch.purchase.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;
import store._0982.elasticsearch.purchase.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.purchase.infrastructure.GroupPurchaseRepository;

@RequiredArgsConstructor
@Service
public class GroupPurchaseSearchService {

    private final ElasticsearchOperations operations;
    private final GroupPurchaseRepository groupPurchaseRepository;

    public boolean createGroupPurchaseIndex() {
        IndexOperations ops = operations.indexOps(GroupPurchaseDocument.class);

        if (!ops.exists()) {
            Document settings = Document.create();
            settings.put("index.number_of_shards", 1);
            settings.put("index.number_of_replicas", 0);
            ops.create(settings);
            ops.putMapping(ops.createMapping(GroupPurchaseDocument.class));
            return true;
        } else {
            ops.putMapping(ops.createMapping(GroupPurchaseDocument.class));
            return false;
        }
    }

    public boolean deleteGroupPurchaseIndex() {
        IndexOperations ops = operations.indexOps(GroupPurchaseDocument.class);
        if (ops.exists()) {
            return ops.delete();
        }
        return false;
    }

    public GroupPurchaseDocument index(GroupPurchaseDocument document) {
        return groupPurchaseRepository.save(document);
    }

    public Page<GroupPurchaseDocument> search(
            String keyword,
            String status,
            int page,
            int size,
            String sort
    ) {
        Pageable pageable = switch (sort) {
            case "price" -> PageRequest.of(page, size,
                    Sort.by(Sort.Direction.ASC, "discountedPrice"));
            case "popular" -> PageRequest.of(page, size,
                    Sort.by(Sort.Direction.DESC, "participants"));
            default -> PageRequest.of(page, size,
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        };

        return groupPurchaseRepository
                .findByTitleContainingOrDescriptionContainingAndStatus(
                        keyword, keyword, status, pageable
                );
    }
}
