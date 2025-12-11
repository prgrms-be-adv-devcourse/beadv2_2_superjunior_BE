package store._0982.common.kafka;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import store._0982.common.kafka.dto.BaseEvent;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class KafkaInterCeptors {
    private static final String TRACE_ID = "traceId";
    private static final String SPAN_ID = "spanId";
    private static final String PARENT_SPAN_ID = "parentSpanId";
    private static final String EVENT_ID = "eventId";

    public static class TracingProducerInterceptor<V extends BaseEvent> implements ProducerInterceptor<String, V> {
        @Override
        public ProducerRecord<String, V> onSend(ProducerRecord<String, V> producerRecord) {
            String traceId = MDC.get(TRACE_ID);
            if (traceId != null) {
                producerRecord.headers().add(TRACE_ID, traceId.getBytes(StandardCharsets.UTF_8));
            }
            String currentSpanId = MDC.get(SPAN_ID);
            if (currentSpanId != null) {
                producerRecord.headers().add(PARENT_SPAN_ID, currentSpanId.getBytes(StandardCharsets.UTF_8));
            }
            UUID eventId = producerRecord.value().getEventId();
            if (eventId != null) {
                producerRecord.headers().add(EVENT_ID, eventId.toString().getBytes(StandardCharsets.UTF_8));
            }
            return producerRecord;
        }

        @Override
        public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
            // 구현할 필요 없음
        }

        @Override
        public void close() {
            // 구현할 필요 없음
        }

        @Override
        public void configure(Map<String, ?> configs) {
            // 구현할 필요 없음
        }
    }

    public static class TracingConsumerInterceptor<V extends BaseEvent> implements ConsumerInterceptor<String, V> {
        @Override
        public ConsumerRecords<String, V> onConsume(ConsumerRecords<String, V> consumerRecords) {
            for (ConsumerRecord<String, V> consumerRecord : consumerRecords) {
                Header eventIdHeader = consumerRecord.headers().lastHeader(EVENT_ID);
                if (eventIdHeader != null) {
                    String eventId = new String(eventIdHeader.value(), StandardCharsets.UTF_8);
                    MDC.put(EVENT_ID, eventId);
                }

                Header parentSpanIdHeader = consumerRecord.headers().lastHeader(PARENT_SPAN_ID);
                if (parentSpanIdHeader != null) {
                    String parentSpanId = new String(parentSpanIdHeader.value(), StandardCharsets.UTF_8);
                    MDC.put(PARENT_SPAN_ID, parentSpanId);
                }
                String newSpanId = String.format("%016x", ThreadLocalRandom.current().nextLong());
                MDC.put(SPAN_ID, newSpanId);

                Header traceIdHeader = consumerRecord.headers().lastHeader(TRACE_ID);
                if (traceIdHeader != null) {
                    String traceId = new String(traceIdHeader.value(), StandardCharsets.UTF_8);
                    MDC.put(TRACE_ID, traceId);
                }
            }
            return consumerRecords;
        }

        @Override
        public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
            // 구현할 필요 없음
        }

        @Override
        public void close() {
            // 구현할 필요 없음
        }

        @Override
        public void configure(Map<String, ?> configs) {
            // 구현할 필요 없음
        }
    }

    private KafkaInterCeptors() {
    }
}
