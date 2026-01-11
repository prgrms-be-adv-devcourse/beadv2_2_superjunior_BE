package store._0982.elasticsearch.reindex;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "reindex.group-purchase")
public class GroupPurchaseReindexProperties {
    private boolean enabled = false;    //수동 runner 재색인 true면 서버 재배포 시 재색인
    private String alias = "group-purchase";
    private int batchSize = 500;
    private boolean switchAlias = true;
}
