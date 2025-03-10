package br.com.jaoafonso.queuemanager.rabbitmq;

import br.com.jaoafonso.queuemanager.rabbitmq.dto.ExchangeDefinition;
import br.com.jaoafonso.queuemanager.rabbitmq.dto.QueueDefinition;
import br.com.jaoafonso.queuemanager.rabbitmq.enums.ERabbitExchange;
import br.com.jaoafonso.queuemanager.rabbitmq.properties.RabbitConfigurationProperties;
import br.com.jaoafonso.queuemanager.rabbitmq.util.QueueFactoryBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;

import static br.com.jaoafonso.queuemanager.rabbitmq.enums.ERabbitExchange.*;
import static br.com.jaoafonso.queuemanager.rabbitmq.util.Constants.DEAD_LETTER_EXCHANGE;
import static br.com.jaoafonso.queuemanager.rabbitmq.util.Constants.DEAD_LETTER_ROUTING_KEY;

@Slf4j
@EnableRabbit
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RabbitConfigurationProperties.class)
public class RabbitConfiguration {

    private final AmqpAdmin amqpAdmin;
    private final ApplicationContext context;
    private final RabbitConfigurationProperties properties;

    @PostConstruct
    public void postConstruct() {
        log.info("Declaring queues based on properties...");
        var exchanges = new HashMap<String, Exchange>();
        properties.getFanouts().forEach(exc -> exchanges.put(exc.getName(), declareExchange(FANOUT, exc)));
        properties.getTopics().forEach(exc -> exchanges.put(exc.getName(), declareExchange(TOPIC, exc)));
        properties.getDirects().forEach(exc -> exchanges.put(exc.getName(), declareExchange(DIRECT, exc)));
        properties.getQueues().forEach(queue -> declareQueue(exchanges.get(queue.getExchange()), queue));
        log.info("Finished declaring queues");
    }

    private void declareQueue(Exchange exchange, QueueDefinition queueDefinition) {
        try {
            var queue = queueDefinition.buildDefaultQueue();
            declareDeadLetterQueue(queue, exchange, queueDefinition);
            insertArguments(queue, queueDefinition);

            amqpAdmin.declareQueue(queue);
            declareBinding(exchange, queue);
            registerBeanInContext(queue);
            log.info("Declared Queue: {} - Exchange: {}", queue.getName(), exchange.getName());
        } catch (Exception ex) {
            log.error("Error while declaring Queue: {} - Exchange: {}", queueDefinition.getName(), exchange.getName(), ex);
        }
    }

    private void declareDeadLetterQueue(Queue queue, Exchange exchange, QueueDefinition queueDefinition) {
        if (queueDefinition.isDeadLetterPattern()) {
            var deadLetterQueue = getDeadLetterQueue(queue, queueDefinition);
            amqpAdmin.declareQueue(deadLetterQueue);
            declareBinding(exchange, deadLetterQueue);
            log.info("Declared Dead Letter Queue: {} - Exchange: {}", deadLetterQueue.getName(), exchange.getName());
        }
    }

    private Exchange declareExchange(ERabbitExchange exchangeType, ExchangeDefinition exchangeDefinition) {
        var exchange = exchangeType.getBuilder().apply(exchangeDefinition);
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }

    private void declareBinding(Exchange exchange, Queue queue) {
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(queue.getName()).noargs());
    }

    private void insertArguments(Queue queue, QueueDefinition queueDefinition) {
        queueDefinition.getArguments().entrySet().stream()
            .filter(entry -> !entry.getKey().equals(DEAD_LETTER_EXCHANGE) && !entry.getKey().equals(DEAD_LETTER_ROUTING_KEY))
            .forEach(entry -> queue.getArguments().put(entry.getKey(), entry.getValue()));
    }

    private Queue getDeadLetterQueue(Queue queue, QueueDefinition queueDefinition) {
        var arguments = queueDefinition.getArguments();
        var exchange = arguments.getOrDefault(DEAD_LETTER_EXCHANGE, "");
        var routingKey = arguments.getOrDefault(DEAD_LETTER_ROUTING_KEY, queueDefinition.getDeadLetterQueueName());

        queue.getArguments().put(DEAD_LETTER_EXCHANGE, exchange);
        queue.getArguments().put(DEAD_LETTER_ROUTING_KEY, routingKey);

        var deadLetterDefinition = queueDefinition.cloneWithNewName(routingKey);
        return new Queue(deadLetterDefinition.getName(), deadLetterDefinition.isDeadLetterDurable());
    }

    private void registerBeanInContext(Queue queue) {
        try {
            var registry = (BeanDefinitionRegistry) context.getAutowireCapableBeanFactory();
            var beanDefinition = new RootBeanDefinition();
            var beanValueConstructor = new ConstructorArgumentValues();
            beanValueConstructor.addGenericArgumentValue(queue);
            beanDefinition.setBeanClass(QueueFactoryBean.class);
            beanDefinition.setConstructorArgumentValues(beanValueConstructor);
            registry.registerBeanDefinition(queue.getName(), beanDefinition);
        } catch (Exception ex) {
            log.error("Failed to register Bean {} into Spring Context.", queue.getName());
        }
    }
}
