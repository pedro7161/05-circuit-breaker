package com.example.provider.model;

import java.math.BigDecimal;

public record PaymentRequest(String orderId, BigDecimal amount, String currency) {
}
