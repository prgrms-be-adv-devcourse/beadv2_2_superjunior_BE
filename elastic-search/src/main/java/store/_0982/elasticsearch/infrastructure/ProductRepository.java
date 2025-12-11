package store._0982.elasticsearch.infrastructure;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import store._0982.elasticsearch.domain.ProductDocument;

public interface ProductRepository extends ElasticsearchRepository<ProductDocument, String> {
}
