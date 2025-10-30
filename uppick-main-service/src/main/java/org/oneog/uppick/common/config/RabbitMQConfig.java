package org.oneog.uppick.common.config;

import java.util.HashMap;
import java.util.Map;

import org.oneog.uppick.domain.auction.event.BidPlacedEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitMQConfig {

    private static final Map<String, Class<?>> EVENT_TYPE_MAPPINGS = new HashMap<>();

    public static final String AUCTION_EXCHANGE_NAME = "auction.exchange";
    public static final String AUCTION_NOTIFICATION_QUEUE = "auction.notification.queue";
    public static final String AUCTION_EVENT_ROUTING_KEY = "auction.#";

    public static final String NOTIFICATION_DLX_EXCHANGE_NAME = "auction.notification.dlx.exchange";
    public static final String NOTIFICATION_DLQ_QUEUE = "auction.notification.dlq.queue";
    public static final String NOTIFICATION_DLQ_ROUTING_KEY = "auction.notification.dlq.routing.key";

    public RabbitMQConfig() {

        EVENT_TYPE_MAPPINGS.put("auction.BidPlacedEvent", BidPlacedEvent.class);
    }

    @Bean
    public TopicExchange auctionExchange() {

        return new TopicExchange(AUCTION_EXCHANGE_NAME);
    }

    @Bean
    public Queue auctionNotificationQueue() {

        return new Queue(AUCTION_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding auctionNotificationBinding(Queue auctionNotificationQueue, TopicExchange auctionExchange) {

        return BindingBuilder.bind(auctionNotificationQueue).to(auctionExchange).with(AUCTION_EVENT_ROUTING_KEY);
    }

    @Bean
    public DirectExchange notificationDLXExchange() {

        return new DirectExchange(NOTIFICATION_DLX_EXCHANGE_NAME);
    }

    @Bean
    public Queue notificationDLQQueue() {

        return new Queue(NOTIFICATION_DLQ_QUEUE, true);
    }

    @Bean
    public Binding notificationDLQBinding(Queue notificationDLQQueue, DirectExchange notificationDLXExchange) {

        return BindingBuilder.bind(notificationDLQQueue).to(notificationDLXExchange).with(NOTIFICATION_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {

        var converter = new Jackson2JsonMessageConverter(objectMapper);
        var typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setIdClassMapping(EVENT_TYPE_MAPPINGS);
        typeMapper.setTypePrecedence(DefaultJackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

}
