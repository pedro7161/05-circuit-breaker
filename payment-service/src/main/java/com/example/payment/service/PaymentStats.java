package com.example.payment.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class PaymentStats {
    private final AtomicLong providerCalls = new AtomicLong();
    private final AtomicLong successfulCalls = new AtomicLong();
    private final AtomicLong failedCalls = new AtomicLong();
    private final AtomicLong fallbackCalls = new AtomicLong();
    public void providerCall(){providerCalls.incrementAndGet();}
    public void success(){successfulCalls.incrementAndGet();}
    public void failure(){failedCalls.incrementAndGet();}
    public void fallback(){fallbackCalls.incrementAndGet();}
    public long providerCalls(){return providerCalls.get();}
    public long successfulCalls(){return successfulCalls.get();}
    public long failedCalls(){return failedCalls.get();}
    public long fallbackCalls(){return fallbackCalls.get();}
    public void reset(){providerCalls.set(0);successfulCalls.set(0);failedCalls.set(0);fallbackCalls.set(0);}
}
