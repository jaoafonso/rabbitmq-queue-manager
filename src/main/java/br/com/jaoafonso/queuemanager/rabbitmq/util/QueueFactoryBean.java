package br.com.jaoafonso.queuemanager.rabbitmq.util;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.FactoryBean;

@RequiredArgsConstructor
public class QueueFactoryBean implements FactoryBean<Queue> {

    private final Queue queue;

    @Override
    public Queue getObject() {
        return queue;
    }

    @Override
    public Class<?> getObjectType() {
        return Queue.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
