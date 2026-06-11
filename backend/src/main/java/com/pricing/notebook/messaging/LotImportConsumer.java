package com.pricing.notebook.messaging;

import com.pricing.notebook.NotebookService;
import com.pricing.shared.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class LotImportConsumer {

    private static final Logger log = LoggerFactory.getLogger(LotImportConsumer.class);

    private final NotebookService notebookService;

    public LotImportConsumer(NotebookService notebookService) {
        this.notebookService = notebookService;
    }

    @RabbitListener(queues = RabbitMQConfig.LOT_IMPORT_QUEUE)
    public void consumeLotImport(LotImportMessage message) {
        log.info("Processando lote: nome={}, custoDev={}, quantidade={}",
                message.nome(), message.custoDev(), message.quantidade());
        notebookService.registerLotPurchase(message.nome(), message.custoDev(), message.quantidade());
        log.info("Lote processado com sucesso: nome={}", message.nome());
    }
}
