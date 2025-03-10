package br.com.jaoafonso.queuemanager.rabbitmq.dto;

import lombok.Data;

@Data
public class ExchangeDefinition {
    private String name = "";
    private boolean durable = true;
    private boolean autoDelete = false;
}
