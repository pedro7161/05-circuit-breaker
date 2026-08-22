package com.example.payment;

import com.example.payment.client.FakePaymentProviderClient;
import com.example.payment.model.PaymentRequest;
import com.example.payment.model.PaymentResponse;
import com.example.payment.model.ProviderPaymentResponse;
import com.example.payment.service.PaymentGateway;
import com.example.payment.service.PaymentStats;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.faulttolerance.api.CircuitBreakerMaintenance;
import io.smallrye.faulttolerance.api.CircuitBreakerState;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class PaymentCircuitBreakerTest {
    @Inject PaymentGateway paymentGateway;
    @Inject PaymentStats stats;
    @Inject CircuitBreakerMaintenance circuitBreakerMaintenance;
    @InjectMock @RestClient FakePaymentProviderClient providerClient;
    private final PaymentRequest request = new PaymentRequest("order-123", new BigDecimal("49.99"), "EUR");

    @BeforeEach void setUp(){reset(providerClient);stats.reset();circuitBreakerMaintenance.resetAll();}

    @Test void healthyProviderReturnsApprovedPayment(){
        when(providerClient.pay(any())).thenReturn(new ProviderPaymentResponse("provider-1","APPROVED"));
        PaymentResponse response=paymentGateway.pay(request);
        assertEquals("APPROVED",response.status());assertFalse(response.fallback());assertEquals(1,stats.providerCalls());assertEquals(CircuitBreakerState.CLOSED,circuitBreakerMaintenance.currentState(PaymentGateway.CIRCUIT_BREAKER_NAME));
    }

    @Test void repeatedProviderFailuresOpenCircuitAndOpenCircuitFailsFast(){
        makeProviderFail();openCircuit();
        assertEquals(CircuitBreakerState.OPEN,circuitBreakerMaintenance.currentState(PaymentGateway.CIRCUIT_BREAKER_NAME));
        long before=stats.providerCalls();long started=System.nanoTime();
        PaymentResponse response=paymentGateway.pay(request);
        long elapsed=(System.nanoTime()-started)/1_000_000;
        assertTrue(response.fallback());assertEquals(before,stats.providerCalls());assertTrue(elapsed<500);
    }

    @Test void providerRecoveryMovesCircuitThroughHalfOpenBackToClosed() throws InterruptedException {
        makeProviderFail();openCircuit();
        when(providerClient.pay(any())).thenReturn(new ProviderPaymentResponse("provider-recovered","APPROVED"));
        Thread.sleep(3200);
        assertFalse(paymentGateway.pay(request).fallback());
        assertEquals(CircuitBreakerState.HALF_OPEN,circuitBreakerMaintenance.currentState(PaymentGateway.CIRCUIT_BREAKER_NAME));
        assertFalse(paymentGateway.pay(request).fallback());
        assertEquals(CircuitBreakerState.CLOSED,circuitBreakerMaintenance.currentState(PaymentGateway.CIRCUIT_BREAKER_NAME));
    }

    @Test void providerFailureUsesFallbackAfterRetryIsExhausted(){
        makeProviderFail();PaymentResponse response=paymentGateway.pay(request);
        assertTrue(response.fallback());assertEquals("UNAVAILABLE",response.status());assertEquals(2,stats.providerCalls());assertEquals(2,stats.failedCalls());assertEquals(1,stats.fallbackCalls());
    }

    private void makeProviderFail(){when(providerClient.pay(any())).thenThrow(new ProcessingException("provider unavailable"));}
    private void openCircuit(){paymentGateway.pay(request);paymentGateway.pay(request);}
}
