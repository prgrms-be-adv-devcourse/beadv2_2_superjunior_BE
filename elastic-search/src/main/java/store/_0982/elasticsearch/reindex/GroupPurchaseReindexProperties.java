package store._0982.elasticsearch.reindex;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "reindex.group-purchase")
public class GroupPurchaseReindexProperties {
    private boolean enabled = false;    //true면 재색인
    private String alias = "group-purchase";
    private int batchSize = 500;
    private boolean switchAlias = true;
}
