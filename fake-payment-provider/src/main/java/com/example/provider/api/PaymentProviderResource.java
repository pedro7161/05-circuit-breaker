package com.example.provider.api;

import com.example.provider.model.PaymentRequest;
import com.example.provider.model.ProviderPaymentResponse;
import com.example.provider.service.ProviderMode;
import com.example.provider.service.ProviderState;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Path("/payments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PaymentProviderResource {
    @Inject ProviderState state;

    @POST
    public ProviderPaymentResponse pay(PaymentRequest request) {
        state.received();
        ProviderMode mode = state.mode();
        if (mode == ProviderMode.SLOW) sleep();
        if (mode == ProviderMode.ALWAYS_FAIL || shouldFailByPercentage(mode)) {
            state.failure();
            throw new ServiceUnavailableException("Fake payment provider failure");
        }
        state.success();
        return new ProviderPaymentResponse(UUID.randomUUID().toString(), "APPROVED");
    }

    private boolean shouldFailByPercentage(ProviderMode mode) {
        return mode == ProviderMode.FAIL_PERCENTAGE && ThreadLocalRandom.current().nextInt(100) < state.failurePercentage();
    }

    private void sleep() {
        try { Thread.sleep(state.slowDelayMillis()); }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            state.failure();
            throw new ServiceUnavailableException("Fake payment provider request was interrupted");
        }
    }
}
