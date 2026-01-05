package store._0982.common.kafka;

/**
 * Kafka 설정에 대한 기본값을 정의한 클래스입니다.
 * <p>필요한 경우 외부 모듈에서 불러와 사용하세요.</p>
 *
 * @author Minhyung Kim
 */
public final class KafkaProperties {
    public static final int DEFAULT_PARTITIONS = 1;
    public static final int DEFAULT_REPLICAS = 1;

    public static final int DEFAULT_CONSUMER_CONCURRENCY = 1;

    public static final String DEFAULT_ACK = "all";

    public static final int MAX_RETRIES = Integer.MAX_VALUE;
    public static final int DELIVERY_TIMEOUT_MS = 120_000;
    public static final long RETRY_BACKOFF_MS = 100L;

    public static final int DEFAULT_BATCH_SIZE = 32768; // 32KB
    public static final int DEFAULT_LINGER_MS = 5;

    public static final String DEFAULT_AUTO_OFFSET_RESET = "earliest";

    private KafkaProperties() {
    }
}
