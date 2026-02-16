package net.javaguides.springboot_jutjubic.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TranscodingConfig {

    public static final String TRANSCODING_QUEUE    = "transcoding.queue";
    public static final String TRANSCODING_EXCHANGE = "transcoding.exchange";
    public static final String TRANSCODING_ROUTING_KEY = "transcoding.key";

    @Bean
    public Queue transcodingQueue() {
        return QueueBuilder.durable(TRANSCODING_QUEUE).build();
    }

    @Bean
    public DirectExchange transcodingExchange() {
        return new DirectExchange(TRANSCODING_EXCHANGE);
    }

    @Bean
    public Binding transcodingBinding(Queue transcodingQueue, DirectExchange transcodingExchange) {
        return BindingBuilder
                .bind(transcodingQueue)
                .to(transcodingExchange)
                .with(TRANSCODING_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }


    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(2);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(1);
        return factory;
    }
}