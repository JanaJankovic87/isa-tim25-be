package net.javaguides.springboot_jutjubic.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String JSON_QUEUE = "video_upload_json";
    public static final String PROTOBUF_QUEUE = "video_upload_protobuf";

    @Bean
    public Queue jsonQueue() {
        return new Queue(JSON_QUEUE, true);
    }

    @Bean
    public Queue protobufQueue() {
        return new Queue(PROTOBUF_QUEUE, true);
    }


    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.declareQueue(jsonQueue());
        admin.declareQueue(protobufQueue());
        return admin;
    }
}