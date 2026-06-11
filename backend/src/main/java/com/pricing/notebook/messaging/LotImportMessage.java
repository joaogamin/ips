package com.pricing.notebook.messaging;

import java.math.BigDecimal;

public record LotImportMessage(String nome, BigDecimal custoDev, Integer quantidade) {}
