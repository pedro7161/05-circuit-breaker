package com.example.payment.model;

public record PaymentStatsResponse(long providerCalls, long successfulCalls, long failedCalls, long fallbackCalls, String circuitState) {}
