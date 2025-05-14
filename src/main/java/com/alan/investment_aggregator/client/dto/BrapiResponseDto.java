package com.alan.investment_aggregator.client.dto;

import java.util.List;

public record BrapiResponseDto(List<StockDto> results) {
}
