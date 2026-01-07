package store._0982.elasticsearch.reindex;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupPurchaseReindexRunner implements ApplicationRunner {

    private final GroupPurchaseReindexService reindexService;
    private final GroupPurchaseReindexProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            log.info("GroupPurchase reindex disabled. Set reindex.group-purchase.enabled=true to run.");
            return;
        }
        reindexService.reindex();
    }
}
