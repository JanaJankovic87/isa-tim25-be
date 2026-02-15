package net.javaguides.springboot_jutjubic.config;

import net.javaguides.springboot_jutjubic.service.impl.WatchPartyRedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    public static final String WATCH_PARTY_CHANNEL = "watch-party-events";

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
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
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter watchPartyMessageListener,
            ChannelTopic watchPartyTopic) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(watchPartyMessageListener, watchPartyTopic);
        return container;
    }
}