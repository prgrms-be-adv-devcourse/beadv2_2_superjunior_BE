package store._0982.elasticsearch.smokesearch;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.infrastructure.queryfactory.GroupPurchaseSearchQueryFactory;

@TestConfiguration
@EnableAutoConfiguration(
        exclude = {
                KafkaAutoConfiguration.class
        }
)
@ComponentScan(
        basePackageClasses = {
                ProductSearchService.class,
                ProductSearchQueryFactory.class,
                GroupPurchaseSearchService.class,
                GroupPurchaseSearchQueryFactory.class
        }
)
public class ElasticsearchTestContext {
}
