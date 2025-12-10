package store._0982.common.kafka;

public final class KafkaProperties {
    public static final int DEFAULT_PARTITIONS = 3;
    public static final int DEFAULT_REPLICAS = 1;

    public static final int DEFAULT_CONSUMER_CONCURRENCY = 3;

    public static final String DEFAULT_ACK = "all";

    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final long RETRY_BACKOFF_MS = 1000L;

    public static final String DEFAULT_AUTO_OFFSET_RESET = "earliest";
    public static final boolean DEFAULT_ENABLE_AUTO_COMMIT = false;

    private KafkaProperties() {
    }
}
