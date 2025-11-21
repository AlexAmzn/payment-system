package com.example.order;

import com.example.payment.PaymentClient;
import com.example.inventory.InventoryClient;
import com.example.notification.NotificationClient;
import com.example.user.UserClient;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

public class OrderService {
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;
    private final NotificationClient notificationClient;
    private final UserClient userClient;
    
    public OrderService(PaymentClient paymentClient, 
                       InventoryClient inventoryClient,
                       NotificationClient notificationClient,
                       UserClient userClient) {
        this.paymentClient = paymentClient;
        this.inventoryClient = inventoryClient;
        this.notificationClient = notificationClient;
        this.userClient = userClient;
    }
    
    public OrderResult processOrder(OrderRequest request) {
        try {
            // Validate user with timeout
            User user = userClient.getUser(request.getUserId())
                .orTimeout(5, TimeUnit.SECONDS)
                .get();
            
            // Check inventory with circuit breaker
            boolean available = inventoryClient.checkAvailability(request.getProductId());
            if (!available) {
                throw new OrderException("Product not available");
            }
            
            // Process payment with retry
            PaymentResult payment = paymentClient.processPayment(
                request.getPaymentInfo(), request.getAmount());
            
            if (payment.isSuccessful()) {
                // Reserve inventory
                inventoryClient.reserveProduct(request.getProductId(), request.getQuantity());
                
                // Send notification asynchronously
                CompletableFuture.runAsync(() -> 
                    notificationClient.sendOrderConfirmation(user.getEmail(), request));
                
                return OrderResult.success(payment.getTransactionId());
            } else {
                return OrderResult.failure("Payment failed: " + payment.getErrorMessage());
            }
            
        } catch (Exception e) {
            // Compensating actions
            rollbackOrder(request);
            return OrderResult.failure("Order processing failed: " + e.getMessage());
        }
    }
    
    private void rollbackOrder(OrderRequest request) {
        try {
            inventoryClient.releaseReservation(request.getProductId(), request.getQuantity());
        } catch (Exception e) {
            // Log rollback failure
            System.err.println("Failed to rollback inventory: " + e.getMessage());
        }
    }
}
