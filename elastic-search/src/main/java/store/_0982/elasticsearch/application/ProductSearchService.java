package store._0982.elasticsearch.application;


import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;
import store._0982.elasticsearch.common.exception.CustomErrorCode;
import store._0982.elasticsearch.common.exception.CustomException;
import store._0982.elasticsearch.domain.ProductDocument;
import store._0982.elasticsearch.infrastructure.ProductRepository;

@RequiredArgsConstructor
@Service
public class ProductSearchService {

    private final ElasticsearchOperations operations;
    private final ProductRepository repository;

    public void createProductIndex() {
        IndexOperations ops = operations.indexOps(ProductDocument.class);

        if (!ops.exists()) {
            Document settings = Document.create();
            settings.put("index.number_of_shards", 1);
            settings.put("index.number_of_replicas", 0);
            ops.create(settings);
            ops.putMapping(ops.createMapping(ProductDocument.class));
        } else {
            throw new CustomException(CustomErrorCode.ALREADY_EXIST_INDEX);
        }
    }

    public void deleteProductIndex() {
        IndexOperations ops = operations.indexOps(ProductDocument.class);
        if (!ops.exists()) {
            throw new CustomException(CustomErrorCode.DONOT_EXIST_INDEX);
        }
        ops.delete();
    }
}
