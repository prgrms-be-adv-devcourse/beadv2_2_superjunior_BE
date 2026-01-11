package store._0982.common.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import store._0982.common.kafka.dto.BaseEvent;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaCommonConfigs {
    /**
     * {@link ProducerFactory}를 빈에 등록할 때 사용하는 메서드입니다.
     * <p><strong>중요한 비즈니스 로직(주문, 결제 등)에 사용하세요.</strong></p>
     * <ul>
     *     <li>ACKS: all (모든 레플리카 응답 대기)</li>
     *     <li>Idempotence: true (중복 전송 방지 활성화)</li>
     * </ul>
     *
     * @param bootStrapServers 브로커의 서버 주소
     * @return 안정성을 최우선으로 설정된 {@link ProducerFactory}
     */
    public static <V extends BaseEvent> ProducerFactory<String, V> defaultProducerFactory(String bootStrapServers) {
        return createProducerFactory(bootStrapServers, KafkaProperties.DEFAULT_ACK, true);
    }

    /**
     * {@link ProducerFactory}를 빈에 등록할 때 사용하는 메서드입니다.
     * <p><strong>단순 알림이나 로그성 데이터 등 일부 유실이 허용되는 로직에 사용하세요.</strong></p>
     * <ul>
     *     <li>ACKS: 0 (응답 대기 안 함, 전송 속도 빠름)</li>
     *     <li>Idempotence: false (중복 전송 방지 비활성화)</li>
     * </ul>
     *
     * @param bootStrapServers 브로커의 서버 주소
     * @return 성능을 최우선으로 설정된 {@link ProducerFactory}
     */
    public static <V extends BaseEvent> ProducerFactory<String, V> fastProducerFactory(String bootStrapServers) {
        return createProducerFactory(bootStrapServers, "0", false);
    }

    /**
     * {@link KafkaTemplate}을 빈에 등록할 때 사용하는 메서드입니다.
     *
     * @param producerFactory 생성한 프로듀서 팩토리
     * @return 기본 설정대로 생성된 {@link KafkaTemplate}
     */
    public static <V extends BaseEvent> KafkaTemplate<String, V> defaultKafkaTemplate(ProducerFactory<String, V> producerFactory) {
        KafkaTemplate<String, V> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setObservationEnabled(true);
        return kafkaTemplate;
    }

    /**
     * {@link ConsumerFactory}를 빈에 등록할 때 사용하는 메서드입니다.
     *
     * @param bootStrapServers 브로커의 서버 주소
     * @param groupId          컨슈머의 그룹 ID
     * @return 기본 설정대로 생성된 {@link ConsumerFactory}
     */
    public static <V extends BaseEvent> ConsumerFactory<String, V> defaultConsumerFactory(String bootStrapServers, String groupId) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaProperties.DEFAULT_AUTO_OFFSET_RESET);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * {@link ConcurrentKafkaListenerContainerFactory}를 빈에 등록할 때 사용하는 메서드입니다.
     *
     * @param consumerFactory 생성한 컨슈머 팩토리
     * @return 기본 설정대로 생성된 {@link ConcurrentKafkaListenerContainerFactory}
     */
    public static <V extends BaseEvent> ConcurrentKafkaListenerContainerFactory<String, V> defaultConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, V> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(KafkaProperties.DEFAULT_CONSUMER_CONCURRENCY);
        factory.getContainerProperties().setObservationEnabled(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
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

    private static <V extends BaseEvent> ProducerFactory<String, V> createProducerFactory(String bootStrapServers, String ack, boolean enableIdempotence) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, ack);
        config.put(ProducerConfig.RETRIES_CONFIG, KafkaProperties.MAX_RETRIES);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, KafkaProperties.DELIVERY_TIMEOUT_MS);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, KafkaProperties.RETRY_BACKOFF_MS);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, KafkaProperties.DEFAULT_BATCH_SIZE);
        config.put(ProducerConfig.LINGER_MS_CONFIG, KafkaProperties.DEFAULT_LINGER_MS);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        return new DefaultKafkaProducerFactory<>(config);
    }
}
