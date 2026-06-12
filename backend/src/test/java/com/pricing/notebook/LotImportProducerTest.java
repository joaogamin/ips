package com.pricing.notebook;

import com.pricing.notebook.messaging.LotImportMessage;
import com.pricing.notebook.messaging.LotImportProducer;
import com.pricing.shared.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LotImportProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private LotImportProducer producer;

    @Test
    void sendLotImport_invocaRabbitTemplateComArgumentosCorretos() {
        LotImportMessage message = new LotImportMessage("Notebook X", new BigDecimal("1200"), 10);

        producer.sendLotImport(message);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.PRICING_EXCHANGE,
                RabbitMQConfig.LOT_IMPORT_ROUTING_KEY,
                message
        );
    }
}
