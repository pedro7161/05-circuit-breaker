package com.example.provider.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class ProviderState {

    private final AtomicReference<ProviderMode> mode = new AtomicReference<>(ProviderMode.NORMAL);
    private final AtomicInteger failurePercentage = new AtomicInteger(50);
    private final AtomicLong slowDelayMillis = new AtomicLong(2000);
    private final AtomicLong receivedCalls = new AtomicLong();
    private final AtomicLong successfulCalls = new AtomicLong();
    private final AtomicLong failedCalls = new AtomicLong();

    public ProviderMode mode() { return mode.get(); }
    public void mode(ProviderMode newMode) { mode.set(newMode); }
    public int failurePercentage() { return failurePercentage.get(); }
    public void failurePercentage(int percentage) { failurePercentage.set(percentage); }
    public long slowDelayMillis() { return slowDelayMillis.get(); }
    public void slowDelayMillis(long delayMillis) { slowDelayMillis.set(delayMillis); }
    public void received() { receivedCalls.incrementAndGet(); }
    public void success() { successfulCalls.incrementAndGet(); }
    public void failure() { failedCalls.incrementAndGet(); }
    public long receivedCalls() { return receivedCalls.get(); }
    public long successfulCalls() { return successfulCalls.get(); }
    public long failedCalls() { return failedCalls.get(); }

    public void resetStats() {
        receivedCalls.set(0);
        successfulCalls.set(0);
        failedCalls.set(0);
    }

    public void resetAll() {
        mode.set(ProviderMode.NORMAL);
        failurePercentage.set(50);
        slowDelayMillis.set(2000);
        resetStats();
    }
}
