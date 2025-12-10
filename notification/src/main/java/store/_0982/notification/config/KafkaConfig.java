package store._0982.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import store._0982.common.kafka.KafkaCommonConfigs;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.kafka.dto.OrderEvent;
import store._0982.common.kafka.dto.PointEvent;
import store._0982.common.kafka.dto.SettlementEvent;
import store._0982.notification.constant.KafkaGroupIds;

@Configuration
public class KafkaConfig {
    @Value("${kafka.server}")
    private String bootStrapServer;

    @Bean
    public ConsumerFactory<String, OrderEvent> orderCreatedEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.ORDER_CREATED_NOTIFICATION);
    }

    @Bean
    public ConsumerFactory<String, OrderEvent> orderChangedEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.ORDER_CHANGED_NOTIFICATION);
    }

    @Bean
    public ConsumerFactory<String, GroupPurchaseEvent> groupPurchaseChangedEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.GROUP_PURCHASE_CHANGED_NOTIFICATION);
    }

    @Bean
    public ConsumerFactory<String, PointEvent> pointChangedEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.POINT_CHANGED_NOTIFICATION);
    }

    @Bean
    public ConsumerFactory<String, PointEvent> pointRechargedEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.POINT_RECHARGED_NOTIFICATION);
    }

    @Bean
    public ConsumerFactory<String, SettlementEvent> dailySettlementCompletedEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.SETTLEMENT_DAILY_COMPLETED_NOTIFICATION);
    }

    @Bean
    public ConsumerFactory<String, SettlementEvent> dailySettlementFailedEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.SETTLEMENT_DAILY_FAILED_NOTIFICATION);
    }

    @Bean
    public ConsumerFactory<String, SettlementEvent> monthlySettlementCompletedEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.SETTLEMENT_MONTHLY_COMPLETED_NOTIFICATION);
    }

    @Bean
    public ConsumerFactory<String, SettlementEvent> monthlySettlementFailedEventConsumerFactory() {
        return KafkaCommonConfigs.defaultConsumerFactory(bootStrapServer, KafkaGroupIds.SETTLEMENT_MONTHLY_FAILED_NOTIFICATION);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> orderCreatedListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(orderCreatedEventConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> orderChangedListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(orderChangedEventConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GroupPurchaseEvent> groupPurchaseChangedListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(groupPurchaseChangedEventConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PointEvent> pointChangedListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(pointChangedEventConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PointEvent> pointRechargedListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(pointRechargedEventConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SettlementEvent> dailySettlementCompletedListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(dailySettlementCompletedEventConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SettlementEvent> dailySettlementFailedListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(dailySettlementFailedEventConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SettlementEvent> monthlySettlementCompletedListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(monthlySettlementCompletedEventConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SettlementEvent> monthlySettlementFailedListenerContainerFactory() {
        return KafkaCommonConfigs.defaultConcurrentKafkaListenerContainerFactory(monthlySettlementFailedEventConsumerFactory());
    }
}
