package com.example.provider.model;

public record ProviderStatsResponse(
        long receivedCalls,
        long successfulCalls,
        long failedCalls,
        String mode,
        int failurePercentage,
        long slowDelayMillis) {
}
