package com.lmf.platform.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Registra Outbox/Inbox comuns. Cada serviço precisa:
 * <ul>
 *   <li>depender de {@code com.lmf:platform-messaging};</li>
 *   <li>incluir {@code com.lmf.platform.messaging} no {@code @EntityScan} e no
 *       {@code @EnableJpaRepositories} (ver {@code PlatformMessagingJpaConfig} de cada serviço);</li>
 *   <li>fornecer um bean {@link OutboxTopicRouter};</li>
 *   <li>adicionar {@code classpath:db/migration/platform} nas locations do Flyway.</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass({EntityManagerFactory.class, KafkaTemplate.class})
@EnableConfigurationProperties(PlatformMessagingProperties.class)
@EnableScheduling
public class PlatformMessagingAutoConfiguration {

    @Bean
    public MessagePublisher messagePublisher(KafkaTemplate<String, String> kafkaTemplate) {
        return new MessagePublisher(kafkaTemplate);
    }

    @Bean
    public OutboxWriter outboxWriter(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        return new OutboxWriter(outboxEventRepository, objectMapper);
    }

    @Bean
    public InboxService inboxService(InboxEventRepository inboxEventRepository) {
        return new InboxService(inboxEventRepository);
    }

    @Bean
    @ConditionalOnBean(OutboxTopicRouter.class)
    public OutboxRelay outboxRelay(OutboxEventRepository outboxEventRepository,
                                   MessagePublisher messagePublisher,
                                   OutboxTopicRouter outboxTopicRouter,
                                   ObjectMapper objectMapper,
                                   PlatformMessagingProperties properties) {
        return new OutboxRelay(outboxEventRepository, messagePublisher, outboxTopicRouter, objectMapper, properties.getDltTopic());
    }
}
