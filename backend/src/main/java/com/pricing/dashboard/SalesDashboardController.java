package com.pricing.dashboard;

import com.pricing.dashboard.dto.SalesDashboardDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class SalesDashboardController {

    private final SalesDashboardService salesDashboardService;

    public SalesDashboardController(SalesDashboardService salesDashboardService) {
        this.salesDashboardService = salesDashboardService;
    }

    @GetMapping
    public ResponseEntity<SalesDashboardDto> getDashboard() {
        return ResponseEntity.ok(salesDashboardService.getDashboard());
    }
}
