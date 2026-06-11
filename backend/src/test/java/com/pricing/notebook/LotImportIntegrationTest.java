package com.pricing.notebook;

import com.pricing.notebook.messaging.LotImportMessage;
import com.pricing.notebook.messaging.LotImportProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest
@Testcontainers
class LotImportIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-alpine");

    @Autowired
    private LotImportProducer producer;

    @Autowired
    private NotebookRepository notebookRepository;

    @Test
    void enviarLote_devePersisteNotebookAposProcessamentoAssíncrono() {
        producer.sendLotImport(new LotImportMessage("TC-Notebook-001", new BigDecimal("500"), 10));

        await().atMost(10, SECONDS).untilAsserted(() -> {
            Optional<Notebook> nb = notebookRepository.findByNome("TC-Notebook-001");
            assertThat(nb).isPresent();
            assertThat(nb.get().getQuantidadeEstoque()).isEqualTo(10);
            assertThat(nb.get().getCustoDev()).isEqualByComparingTo(new BigDecimal("500"));
        });
    }

    @Test
    void enviarLoteDuplicado_deveSomarEstoque() {
        producer.sendLotImport(new LotImportMessage("TC-Notebook-002", new BigDecimal("800"), 5));
        await().atMost(10, SECONDS).untilAsserted(() ->
                assertThat(notebookRepository.findByNome("TC-Notebook-002")).isPresent());

        producer.sendLotImport(new LotImportMessage("TC-Notebook-002", new BigDecimal("800"), 3));
        await().atMost(10, SECONDS).untilAsserted(() -> {
            Optional<Notebook> nb = notebookRepository.findByNome("TC-Notebook-002");
            assertThat(nb).isPresent();
            assertThat(nb.get().getQuantidadeEstoque()).isEqualTo(8);
        });
    }
}
