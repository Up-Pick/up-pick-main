package org.oneog.uppick.auction.common.config;

import java.util.HashMap;
import java.util.Map;

import org.oneog.uppick.auction.domain.auction.event.BidPlacedEvent;
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

    public RabbitMQConfig() {

        EVENT_TYPE_MAPPINGS.put("auction.BidPlacedEvent", BidPlacedEvent.class);
    }

    @Bean
    public TopicExchange auctionExchange() {

        return new TopicExchange(AUCTION_EXCHANGE_NAME);
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
