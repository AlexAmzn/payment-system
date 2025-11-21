package com.example.payment;

import java.util.concurrent.TimeUnit;
import java.util.Random;

public class PaymentClient {
    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 1000;
    private final Random random = new Random();
    
    public PaymentResult processPayment(PaymentInfo paymentInfo, double amount) {
        return executeWithRetry(() -> doProcessPayment(paymentInfo, amount), MAX_RETRIES);
    }
    
    private PaymentResult doProcessPayment(PaymentInfo paymentInfo, double amount) {
        // Simulate external payment service call
        if (random.nextDouble() < 0.3) { // 30% failure rate
            throw new PaymentException("Payment service temporarily unavailable");
        }
        
        if (paymentInfo.getCardNumber().startsWith("4000")) {
            throw new PaymentException("Invalid card number");
        }
        
        return new PaymentResult(true, "txn_" + System.currentTimeMillis(), null);
    }
    
    private <T> T executeWithRetry(RetryableOperation<T> operation, int maxRetries) {
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt == maxRetries) {
                    break; // No more retries
                }
                
                // Exponential backoff with jitter
                long delay = BASE_DELAY_MS * (1L << attempt);
                long jitter = (long) (delay * 0.1 * random.nextDouble());
                
                try {
                    TimeUnit.MILLISECONDS.sleep(delay + jitter);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new PaymentException("Payment retry interrupted", ie);
                }
            }
        }
        
        throw new PaymentException("Payment failed after " + maxRetries + " retries", lastException);
    }
    
    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }
}
