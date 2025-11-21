package com.example.model;

// User models
class User {
    private final String id;
    private final String email;
    private final String name;
    
    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }
    
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
}

// Order models
class OrderRequest {
    private final String userId;
    private final String productId;
    private final int quantity;
    private final PaymentInfo paymentInfo;
    private final double amount;
    
    public OrderRequest(String userId, String productId, int quantity, PaymentInfo paymentInfo, double amount) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.paymentInfo = paymentInfo;
        this.amount = amount;
    }
    
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public PaymentInfo getPaymentInfo() { return paymentInfo; }
    public double getAmount() { return amount; }
}

class OrderResult {
    private final boolean success;
    private final String transactionId;
    private final String errorMessage;
    
    private OrderResult(boolean success, String transactionId, String errorMessage) {
        this.success = success;
        this.transactionId = transactionId;
        this.errorMessage = errorMessage;
    }
    
    public static OrderResult success(String transactionId) {
        return new OrderResult(true, transactionId, null);
    }
    
    public static OrderResult failure(String errorMessage) {
        return new OrderResult(false, null, errorMessage);
    }
    
    public boolean isSuccess() { return success; }
    public String getTransactionId() { return transactionId; }
    public String getErrorMessage() { return errorMessage; }
}

// Payment models
class PaymentInfo {
    private final String cardNumber;
    private final String expiryDate;
    private final String cvv;
    
    public PaymentInfo(String cardNumber, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }
    
    public String getCardNumber() { return cardNumber; }
    public String getExpiryDate() { return expiryDate; }
    public String getCvv() { return cvv; }
}

class PaymentResult {
    private final boolean successful;
    private final String transactionId;
    private final String errorMessage;
    
    public PaymentResult(boolean successful, String transactionId, String errorMessage) {
        this.successful = successful;
        this.transactionId = transactionId;
        this.errorMessage = errorMessage;
    }
    
    public boolean isSuccessful() { return successful; }
    public String getTransactionId() { return transactionId; }
    public String getErrorMessage() { return errorMessage; }
}

// Exception classes
class OrderException extends RuntimeException {
    public OrderException(String message) { super(message); }
    public OrderException(String message, Throwable cause) { super(message, cause); }
}

class PaymentException extends RuntimeException {
    public PaymentException(String message) { super(message); }
    public PaymentException(String message, Throwable cause) { super(message, cause); }
}

class InventoryException extends RuntimeException {
    public InventoryException(String message) { super(message); }
    public InventoryException(String message, Throwable cause) { super(message, cause); }
}

class NotificationException extends RuntimeException {
    public NotificationException(String message) { super(message); }
    public NotificationException(String message, Throwable cause) { super(message, cause); }
}

class UserServiceException extends RuntimeException {
    public UserServiceException(String message) { super(message); }
    public UserServiceException(String message, Throwable cause) { super(message, cause); }
}

class UserNotFoundException extends UserServiceException {
    public UserNotFoundException(String message) { super(message); }
}
