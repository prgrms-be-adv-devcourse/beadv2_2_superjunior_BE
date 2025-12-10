package store._0982.common.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 각 모듈에서 Kafka 설정을 빈에 등록할 때 사용됩니다.
 * <p>따로 설정값을 지정하지 말고 여기에 정의된 메서드를 사용해 주세요.</p>
 * <pre>
 * {@code
 * @Configuration
 * public class KafkaConfig {
 *     @Bean
 *     public ProducerFactory<String, CustomEvent> customProducerFactory() {
 *         return KafkaCommonConfigs.defaultProducerFactory(브로커 서버 주소);
 *     }
 *     ...
 * }}
 * </pre>
 *
 * @author Minhyung Kim
 */
@SuppressWarnings("unused")
public final class KafkaCommonConfigs {
    /**
     * {@link ProducerFactory}를 빈에 등록할 때 사용하는 메서드입니다.
     *
     * @param bootStrapServers 브로커의 서버 주소
     * @return 기본 설정대로 생성된 {@link ProducerFactory}
     */
    public static <V> ProducerFactory<String, V> defaultProducerFactory(String bootStrapServers) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, KafkaProperties.DEFAULT_ACK);
        config.put(ProducerConfig.RETRIES_CONFIG, KafkaProperties.MAX_RETRY_ATTEMPTS);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, KafkaProperties.RETRY_BACKOFF_MS);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * {@link KafkaTemplate}을 빈에 등록할 때 사용하는 메서드입니다.
     *
     * @param producerFactory 생성한 프로듀서 팩토리
     * @return 기본 설정대로 생성된 {@link KafkaTemplate}
     */
    public static <V> KafkaTemplate<String, V> defaultKafkaTemplate(ProducerFactory<String, V> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * {@link ConsumerFactory}를 빈에 등록할 때 사용하는 메서드입니다.
     *
     * @param bootStrapServers 브로커의 서버 주소
     * @param groupId          컨슈머의 그룹 ID
     * @return 기본 설정대로 생성된 {@link ConsumerFactory}
     */
    public static <V> ConsumerFactory<String, V> defaultConsumerFactory(String bootStrapServers, String groupId) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaProperties.DEFAULT_AUTO_OFFSET_RESET);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, KafkaProperties.DEFAULT_ENABLE_AUTO_COMMIT);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * {@link ConcurrentKafkaListenerContainerFactory}를 빈에 등록할 때 사용하는 메서드입니다.
     *
     * @param consumerFactory 생성한 컨슈머 팩토리
     * @return 기본 설정대로 생성된 {@link ConcurrentKafkaListenerContainerFactory}
     */
    public static <V> ConcurrentKafkaListenerContainerFactory<String, V> defaultConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, V> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(KafkaProperties.DEFAULT_CONSUMER_CONCURRENCY);
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    /**
     * {@link NewTopic}을 빈에 등록할 때 사용하는 메서드입니다.
     * <p>토픽 등록은 해당 토픽에 대한 이벤트를 발행하는 모듈에서만 해주세요.<br>
     * 예시) ORDER_CREATED는 order 모듈, POINT_RECHARGED는 point 모듈에서 생성</p>
     *
     * @param topicName {@link KafkaTopics}에 정의된 값만 이용해 주세요.
     * @return 기본 설정대로 생성된 {@link NewTopic}
     */
    public static NewTopic createTopic(String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(KafkaProperties.DEFAULT_PARTITIONS)
                .replicas(KafkaProperties.DEFAULT_REPLICAS)
                .build();
    }

    /**
     * {@link NewTopic}을 빈에 등록할 때 사용하는 메서드입니다.
     * <p>직접 파티션과 레플리카 수를 설정할 수 있습니다.</p>
     * <p>토픽 등록은 해당 토픽에 대한 이벤트를 발행하는 모듈에서만 해주세요.<br>
     * 예시) ORDER_CREATED는 order 모듈, POINT_RECHARGED는 point 모듈에서 생성</p>
     *
     * @param topicName  {@link KafkaTopics}에 정의된 값만 이용해 주세요.
     * @param partitions 설정할 파티션 개수
     * @param replicas   설정할 레플리카 개수
     * @return 지정한 설정대로 생성된 {@link NewTopic}
     */
    public static NewTopic createTopic(String topicName, int partitions, int replicas) {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    private KafkaCommonConfigs() {
    }
}
