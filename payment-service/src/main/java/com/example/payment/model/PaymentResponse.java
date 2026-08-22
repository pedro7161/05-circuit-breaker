package com.example.payment.model;

public record PaymentResponse(String orderId, String status, String providerReference, boolean fallback, String message) {}
