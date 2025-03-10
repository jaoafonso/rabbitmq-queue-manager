package br.com.jaoafonso.queuemanager.rabbitmq.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
}
