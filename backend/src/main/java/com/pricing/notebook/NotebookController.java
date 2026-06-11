package com.pricing.notebook;

import com.pricing.notebook.dto.LotPurchaseRequest;
import com.pricing.notebook.dto.NotebookResponseDto;
import com.pricing.notebook.messaging.LotImportMessage;
import com.pricing.notebook.messaging.LotImportProducer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notebooks")
public class NotebookController {

    private final NotebookService notebookService;
    private final LotImportProducer lotImportProducer;

    public NotebookController(NotebookService notebookService, LotImportProducer lotImportProducer) {
        this.notebookService = notebookService;
        this.lotImportProducer = lotImportProducer;
    }

    @GetMapping
    public ResponseEntity<List<NotebookResponseDto>> findAll() {
        return ResponseEntity.ok(notebookService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotebookResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(notebookService.findById(id));
    }

    @PostMapping("/lote")
    public ResponseEntity<String> importLot(@Valid @RequestBody LotPurchaseRequest request) {
        lotImportProducer.sendLotImport(
                new LotImportMessage(request.nome(), request.custoDev(), request.quantidade())
        );
        return ResponseEntity.accepted().body("Lote enviado para processamento.");
    }
}
