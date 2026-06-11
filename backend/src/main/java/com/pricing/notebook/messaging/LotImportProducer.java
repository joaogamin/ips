package com.pricing.notebook.messaging;

import com.pricing.shared.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class LotImportProducer {

    private static final Logger log = LoggerFactory.getLogger(LotImportProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public LotImportProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendLotImport(LotImportMessage message) {
        log.info("Publicando lote na fila: nome={}, custoDev={}, quantidade={}",
                message.nome(), message.custoDev(), message.quantidade());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PRICING_EXCHANGE,
                RabbitMQConfig.LOT_IMPORT_ROUTING_KEY,
                message
        );
    }
}
