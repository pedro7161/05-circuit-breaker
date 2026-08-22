package com.example.payment.client;

import com.example.payment.model.PaymentRequest;
import com.example.payment.model.ProviderPaymentResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/payments")
@RegisterRestClient(configKey = "fake-payment-provider")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface FakePaymentProviderClient {
    @POST ProviderPaymentResponse pay(PaymentRequest request);
}
