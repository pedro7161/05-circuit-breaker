package com.example.payment.api;

import com.example.payment.model.PaymentStatsResponse;
import com.example.payment.service.PaymentGateway;
import com.example.payment.service.PaymentStats;
import io.smallrye.faulttolerance.api.CircuitBreakerMaintenance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {
    @Inject PaymentStats stats;
    @Inject CircuitBreakerMaintenance circuitBreakerMaintenance;

    @GET @Path("/stats")
    public PaymentStatsResponse stats() {
        return new PaymentStatsResponse(stats.providerCalls(), stats.successfulCalls(), stats.failedCalls(), stats.fallbackCalls(), circuitBreakerMaintenance.currentState(PaymentGateway.CIRCUIT_BREAKER_NAME).name());
    }

    @POST @Path("/reset")
    public PaymentStatsResponse reset() {
        stats.reset();
        circuitBreakerMaintenance.reset(PaymentGateway.CIRCUIT_BREAKER_NAME);
        return stats();
    }
}
