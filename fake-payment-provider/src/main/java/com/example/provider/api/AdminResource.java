package com.example.provider.api;

import com.example.provider.model.ProviderStatsResponse;
import com.example.provider.service.ProviderMode;
import com.example.provider.service.ProviderState;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Locale;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {
    @Inject ProviderState state;

    @PUT @Path("/mode/{mode}")
    public ProviderStatsResponse mode(@PathParam("mode") String mode) {
        try { state.mode(ProviderMode.valueOf(mode.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { throw new BadRequestException("Unknown mode: " + mode); }
        return stats();
    }

    @PUT @Path("/failure-percentage/{percentage}")
    public ProviderStatsResponse failurePercentage(@PathParam("percentage") int percentage) {
        if (percentage < 0 || percentage > 100) throw new BadRequestException("Failure percentage must be between 0 and 100");
        state.failurePercentage(percentage);
        return stats();
    }

    @PUT @Path("/slow-delay/{delayMillis}")
    public ProviderStatsResponse slowDelay(@PathParam("delayMillis") long delayMillis) {
        if (delayMillis < 0) throw new BadRequestException("Slow delay must be >= 0");
        state.slowDelayMillis(delayMillis);
        return stats();
    }

    @GET @Path("/stats")
    public ProviderStatsResponse stats() {
        return new ProviderStatsResponse(state.receivedCalls(), state.successfulCalls(), state.failedCalls(), state.mode().name(), state.failurePercentage(), state.slowDelayMillis());
    }

    @POST @Path("/reset")
    public ProviderStatsResponse reset() { state.resetAll(); return stats(); }
}
