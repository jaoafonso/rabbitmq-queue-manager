package br.com.jaoafonso.queuemanager.rabbitmq.properties;

import br.com.jaoafonso.queuemanager.rabbitmq.dto.ExchangeDefinition;
import br.com.jaoafonso.queuemanager.rabbitmq.dto.QueueDefinition;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "queue-manager.rabbitmq")
public class RabbitConfigurationProperties {
    private List<ExchangeDefinition> directs = new ArrayList<>();
    private List<ExchangeDefinition> fanouts = new ArrayList<>();
    private List<ExchangeDefinition> topics = new ArrayList<>();
    private List<QueueDefinition> queues = new ArrayList<>();
}
