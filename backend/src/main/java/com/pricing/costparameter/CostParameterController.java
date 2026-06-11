package com.pricing.costparameter;

import com.pricing.costparameter.dto.CostParameterResponse;
import com.pricing.costparameter.dto.UpdateCostParameterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parametros")
public class CostParameterController {

    private final CostParameterService service;

    public CostParameterController(CostParameterService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CostParameterResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CostParameterResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCostParameterRequest request) {
        return ResponseEntity.ok(service.update(id, request.valor()));
    }
}
