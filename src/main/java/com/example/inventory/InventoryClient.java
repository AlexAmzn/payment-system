package com.example.inventory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class InventoryClient {
    private final CircuitBreaker circuitBreaker;
    
    public InventoryClient() {
        this.circuitBreaker = new CircuitBreaker(5, 60000, 30000); // 5 failures, 60s timeout, 30s half-open
    }
    
    public boolean checkAvailability(String productId) {
        return circuitBreaker.execute(() -> doCheckAvailability(productId));
    }
    
    public void reserveProduct(String productId, int quantity) {
        circuitBreaker.execute(() -> {
            doReserveProduct(productId, quantity);
            return null;
        });
    }
    
    public void releaseReservation(String productId, int quantity) {
        circuitBreaker.execute(() -> {
            doReleaseReservation(productId, quantity);
            return null;
        });
    }
    
    private boolean doCheckAvailability(String productId) {
        // Simulate inventory service call
        if (Math.random() < 0.2) { // 20% failure rate
            throw new InventoryException("Inventory service unavailable");
        }
        return !productId.equals("OUT_OF_STOCK");
    }
    
    private void doReserveProduct(String productId, int quantity) {
        if (Math.random() < 0.15) { // 15% failure rate
            throw new InventoryException("Failed to reserve product");
        }
        // Simulate reservation logic
    }
    
    private void doReleaseReservation(String productId, int quantity) {
        if (Math.random() < 0.1) { // 10% failure rate
            throw new InventoryException("Failed to release reservation");
        }
        // Simulate release logic
    }
    
    private static class CircuitBreaker {
        private enum State { CLOSED, OPEN, HALF_OPEN }
        
        private final int failureThreshold;
        private final long timeoutMs;
        private final long retryTimeoutMs;
        
        private volatile State state = State.CLOSED;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong lastFailureTime = new AtomicLong(0);
        
        public CircuitBreaker(int failureThreshold, long timeoutMs, long retryTimeoutMs) {
            this.failureThreshold = failureThreshold;
            this.timeoutMs = timeoutMs;
            this.retryTimeoutMs = retryTimeoutMs;
        }
        
        public <T> T execute(CircuitBreakerOperation<T> operation) {
            if (state == State.OPEN) {
                if (System.currentTimeMillis() - lastFailureTime.get() > retryTimeoutMs) {
                    state = State.HALF_OPEN;
                } else {
                    throw new InventoryException("Circuit breaker is OPEN");
                }
            }
            
            try {
                T result = operation.execute();
                onSuccess();
                return result;
            } catch (Exception e) {
                onFailure();
                throw e;
            }
        }
        
        private void onSuccess() {
            failureCount.set(0);
            state = State.CLOSED;
        }
        
        private void onFailure() {
            int failures = failureCount.incrementAndGet();
            lastFailureTime.set(System.currentTimeMillis());
            
            if (failures >= failureThreshold) {
                state = State.OPEN;
            }
        }
        
        @FunctionalInterface
        private interface CircuitBreakerOperation<T> {
            T execute() throws Exception;
        }
    }
}
