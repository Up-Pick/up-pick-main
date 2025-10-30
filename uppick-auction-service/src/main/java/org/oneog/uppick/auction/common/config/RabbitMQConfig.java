package org.oneog.uppick.auction.common.config;

import java.util.HashMap;
import java.util.Map;

import org.oneog.uppick.auction.domain.auction.event.AuctionEndedEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
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

    public static final String AUCTION_ENDED_EXCHANGE_NAME = "auction.ended.exchange";
    public static final String AUCTION_ENDED_QUEUE = "auction.ended.queue";
    public static final String AUCTION_ENDED_ROUTING_KEY = "auction.ended.#";

    public static final String AUCTION_DLX_EXCHANGE_NAME = "auction.dlx.exchange";
    public static final String AUCTION_DLQ_QUEUE = "auction.dlq.queue";
    public static final String AUCTION_DLQ_ROUTING_KEY = "auction.dlq.routing.key";

    public RabbitMQConfig() {

        EVENT_TYPE_MAPPINGS.put("auction.BidPlacedEvent", AuctionEndedEvent.class);
        EVENT_TYPE_MAPPINGS.put("auction.AuctionEndedEvent", AuctionEndedEvent.class);
    }

    @Bean
    public TopicExchange auctionExchange() {

        return new TopicExchange(AUCTION_EXCHANGE_NAME);
    }

    @Bean
    public CustomExchange auctionEndedExchange() {

        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "topic");
        args.put("x-dead-letter-exchange", AUCTION_DLX_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", AUCTION_DLQ_ROUTING_KEY);
        return new CustomExchange(AUCTION_ENDED_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue auctionEndedQueue() {

        return new Queue(AUCTION_ENDED_QUEUE, true);
    }

    @Bean
    public Binding auctionEndedBinding(Queue auctionEndedQueue, CustomExchange auctionEndedExchange) {

        return BindingBuilder.bind(auctionEndedQueue)
            .to(auctionEndedExchange)
            .with(AUCTION_ENDED_ROUTING_KEY)
            .noargs();
    }

    @Bean
    public DirectExchange auctionDLXExchange() {

        return new DirectExchange(AUCTION_DLX_EXCHANGE_NAME);
    }

    @Bean
    public Queue auctionDLQQueue() {

        return new Queue(AUCTION_DLQ_QUEUE, true);
    }

    @Bean
    public Binding auctionDLQBinding(Queue auctionDLQQueue, DirectExchange auctionDLXExchange) {

        return BindingBuilder.bind(auctionDLQQueue)
            .to(auctionDLXExchange)
            .with(AUCTION_DLQ_ROUTING_KEY);
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
