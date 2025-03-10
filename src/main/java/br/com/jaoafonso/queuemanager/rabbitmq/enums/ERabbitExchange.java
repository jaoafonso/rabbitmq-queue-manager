package br.com.jaoafonso.queuemanager.rabbitmq.enums;

import br.com.jaoafonso.queuemanager.rabbitmq.dto.ExchangeDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;

import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum ERabbitExchange {

    FANOUT(
        exchange -> new FanoutExchange(exchange.getName(), exchange.isDurable(), exchange.isAutoDelete())),
    TOPIC(
        exchange -> new TopicExchange(exchange.getName(), exchange.isDurable(), exchange.isAutoDelete())),
    DIRECT(
        exchange -> new DirectExchange(exchange.getName(), exchange.isDurable(), exchange.isAutoDelete()));

    private final Function<ExchangeDefinition, AbstractExchange> builder;
}
