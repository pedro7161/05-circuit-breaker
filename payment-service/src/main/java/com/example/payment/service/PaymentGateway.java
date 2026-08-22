package com.example.payment.service;

import com.example.payment.client.FakePaymentProviderClient;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentResponse;
import com.example.payment.model.ProviderPaymentResponse;
import io.smallrye.faulttolerance.api.CircuitBreakerName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class PaymentGateway {
    public static final String CIRCUIT_BREAKER_NAME = "payment-provider";
    @Inject @RestClient FakePaymentProviderClient providerClient;
    @Inject PaymentStats stats;

    @Timeout(value = 750, unit = ChronoUnit.MILLIS)
    @Retry(maxRetries = 1, delay = 100, delayUnit = ChronoUnit.MILLIS, jitter = 0,
           retryOn = {WebApplicationException.class, ProcessingException.class, TimeoutException.class},
           abortOn = CircuitBreakerOpenException.class)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 3, delayUnit = ChronoUnit.SECONDS, successThreshold = 2)
    @CircuitBreakerName(CIRCUIT_BREAKER_NAME)
    @Fallback(fallbackMethod = "fallback")
    public PaymentResponse pay(PaymentRequest request) {
        stats.providerCall();
        try {
            ProviderPaymentResponse response = providerClient.pay(request);
            stats.success();
            return new PaymentResponse(request.orderId(), response.status(), response.providerReference(), false, "Payment provider call succeeded");
        } catch (RuntimeException exception) {
            stats.failure();
            throw exception;
        }
    }

    public PaymentResponse fallback(PaymentRequest request) {
        stats.fallback();
        return new PaymentResponse(request.orderId(), "UNAVAILABLE", null, true, "Payment provider is temporarily unavailable; fallback response returned");
    }
}
