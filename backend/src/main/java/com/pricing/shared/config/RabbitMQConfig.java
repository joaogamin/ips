package com.pricing.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String LOT_IMPORT_QUEUE = "lot-import.queue";
    public static final String PRICING_EXCHANGE = "pricing.exchange";
    public static final String LOT_IMPORT_ROUTING_KEY = "lot.import";

    public static final String LOT_IMPORT_DLQ = "lot-import.dlq";
    public static final String LOT_IMPORT_DLX = "lot-import.dlx";

    @Bean
    public Queue lotImportQueue() {
        return QueueBuilder.durable(LOT_IMPORT_QUEUE)
                .withArgument("x-dead-letter-exchange", LOT_IMPORT_DLX)
                .withArgument("x-dead-letter-routing-key", LOT_IMPORT_DLQ)
                .build();
    }

    @Bean
    public Queue lotImportDeadLetterQueue() {
        return QueueBuilder.durable(LOT_IMPORT_DLQ).build();
    }

    @Bean
    public DirectExchange pricingExchange() {
        return new DirectExchange(PRICING_EXCHANGE);
    }

    @Bean
    public DirectExchange lotImportDeadLetterExchange() {
        return new DirectExchange(LOT_IMPORT_DLX);
    }

    @Bean
    public Binding lotImportBinding(Queue lotImportQueue, DirectExchange pricingExchange) {
        return BindingBuilder.bind(lotImportQueue).to(pricingExchange).with(LOT_IMPORT_ROUTING_KEY);
    }

    @Bean
    public Binding lotImportDlqBinding(Queue lotImportDeadLetterQueue, DirectExchange lotImportDeadLetterExchange) {
        return BindingBuilder.bind(lotImportDeadLetterQueue).to(lotImportDeadLetterExchange).with(LOT_IMPORT_DLQ);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
