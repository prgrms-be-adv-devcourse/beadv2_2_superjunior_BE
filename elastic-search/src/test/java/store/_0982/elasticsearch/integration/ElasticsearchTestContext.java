package store._0982.elasticsearch.integration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import store._0982.elasticsearch.application.ProductSearchService;

@TestConfiguration
@EnableAutoConfiguration(
        exclude = {
                KafkaAutoConfiguration.class
        }
)
@ComponentScan(
        basePackageClasses = {
                ProductSearchService.class
        }
)
public class ElasticsearchTestContext {
}
