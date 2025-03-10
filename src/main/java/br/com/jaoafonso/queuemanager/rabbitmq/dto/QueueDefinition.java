package br.com.jaoafonso.queuemanager.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.var;
import org.springframework.amqp.core.Queue;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueDefinition {
    private String name = "";
    private String exchange = "";
    private boolean durable = false;
    private boolean exclusive = false;
    private boolean autoDelete = false;
    private boolean deadLetterPattern = true;
    private boolean deadLetterDurable = true;
    private Map<String, String> arguments = new HashMap<>();

    public String getDeadLetterQueueName() {
        var baseIndex = name.indexOf(".queue");
        var baseName = name.substring(0, baseIndex);
        return baseName + "-failure.queue";
    }

    public QueueDefinition cloneWithNewName(String newName) {
        return new QueueDefinition(
            newName,
            this.exchange,
            this.durable,
            this.exclusive,
            this.autoDelete,
            this.deadLetterPattern,
            this.deadLetterDurable,
            this.arguments
        );
    }

    public Queue buildDefaultQueue() {
        return new Queue(this.name, this.durable, this.exclusive, this.autoDelete, new HashMap<>());
    }
}
