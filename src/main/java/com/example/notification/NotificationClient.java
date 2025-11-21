package com.example.notification;

import java.util.concurrent.*;

public class NotificationClient {
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private static final int TIMEOUT_SECONDS = 10;
    
    public void sendOrderConfirmation(String email, Object orderRequest) {
        CompletableFuture<Void> emailFuture = sendEmailAsync(email, "Order Confirmation", 
            "Your order has been confirmed: " + orderRequest.toString());
        
        CompletableFuture<Void> smsFuture = sendSmsAsync(extractPhoneFromEmail(email), 
            "Order confirmed! Check your email for details.");
        
        // Wait for both with timeout
        try {
            CompletableFuture.allOf(emailFuture, smsFuture)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .get();
        } catch (TimeoutException e) {
            System.err.println("Notification timeout - some notifications may not have been sent");
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
    }
    
    public CompletableFuture<Void> sendEmailAsync(String email, String subject, String body) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Simulate email service call with potential delay
                TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 5000 + 1000)); // 1-6 seconds
                
                if (Math.random() < 0.1) { // 10% failure rate
                    throw new NotificationException("Email service unavailable");
                }
                
                System.out.println("Email sent to: " + email);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NotificationException("Email sending interrupted", e);
            }
        }, executor);
    }
    
    public CompletableFuture<Void> sendSmsAsync(String phoneNumber, String message) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Simulate SMS service call with potential delay
                TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 3000 + 500)); // 0.5-3.5 seconds
                
                if (Math.random() < 0.15) { // 15% failure rate
                    throw new NotificationException("SMS service unavailable");
                }
                
                System.out.println("SMS sent to: " + phoneNumber);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NotificationException("SMS sending interrupted", e);
            }
        }, executor);
    }
    
    private String extractPhoneFromEmail(String email) {
        // Simplified phone extraction logic
        return "+1555" + Math.abs(email.hashCode() % 10000);
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
