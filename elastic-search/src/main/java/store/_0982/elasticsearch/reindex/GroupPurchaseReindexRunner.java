package store._0982.elasticsearch.reindex;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import store._0982.elasticsearch.application.reindex.GroupPurchaseReindexService;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupPurchaseReindexRunner implements ApplicationRunner {

    private final GroupPurchaseReindexService reindexService;
    private final GroupPurchaseReindexProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        reindexService.reindex();
    }
}
