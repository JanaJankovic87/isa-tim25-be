package net.javaguides.springboot_jutjubic.config;

import net.javaguides.springboot_jutjubic.service.impl.WatchPartyRedisSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    public static final String WATCH_PARTY_CHANNEL = "watch-party-events";

    // === STREAMING REDIS (PRIMARY) ===
    @Primary
    @Bean(name = "streamingRedisConnectionFactory")
    public RedisConnectionFactory streamingRedisConnectionFactory(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.database}") int database) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);

        return new JedisConnectionFactory(config);
    }

    @Primary
    @Bean(name = "streamingRedisTemplate")
    public RedisTemplate<String, Object> streamingRedisTemplate(
            @Qualifier("streamingRedisConnectionFactory") RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // === WATCH PARTY PUB/SUB ===
    @Bean
    public StringRedisTemplate stringRedisTemplate(
            @Qualifier("streamingRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public ChannelTopic watchPartyTopic() {
        return new ChannelTopic(WATCH_PARTY_CHANNEL);
    }

    @Bean
    public MessageListenerAdapter watchPartyMessageListener(WatchPartyRedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            @Qualifier("streamingRedisConnectionFactory") RedisConnectionFactory connectionFactory,
            MessageListenerAdapter watchPartyMessageListener,
            ChannelTopic watchPartyTopic) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(watchPartyMessageListener, watchPartyTopic);
        return container;
    }

    // === MONITORING REDIS (SECONDARY) ===
    @Bean(name = "monitoringRedisConnectionFactory")
    public RedisConnectionFactory monitoringRedisConnectionFactory(
            @Value("${monitoring.redis.host}") String host,
            @Value("${monitoring.redis.port}") int port,
            @Value("${monitoring.redis.database}") int database) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);

        return new JedisConnectionFactory(config);
    }

    @Bean(name = "monitoringRedisTemplate")
    public RedisTemplate<String, Object> monitoringRedisTemplate(
            @Qualifier("monitoringRedisConnectionFactory") RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
