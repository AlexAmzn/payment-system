package com.example.user;

import java.util.concurrent.*;
import java.util.Optional;

public class UserClient {
    private final ConcurrentHashMap<String, CachedUser> userCache = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final long CACHE_TTL_MS = 300000; // 5 minutes
    private static final int TIMEOUT_SECONDS = 5;
    
    public CompletableFuture<Optional<User>> getUser(String userId) {
        // Check cache first
        CachedUser cached = userCache.get(userId);
        if (cached != null && !cached.isExpired()) {
            return CompletableFuture.completedFuture(Optional.of(cached.user));
        }
        
        // Fetch from service with timeout
        return CompletableFuture
            .supplyAsync(() -> fetchUserFromService(userId), executor)
            .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .handle((user, throwable) -> {
                if (throwable != null) {
                    if (throwable instanceof TimeoutException) {
                        System.err.println("User service timeout for userId: " + userId);
                    } else {
                        System.err.println("User service error: " + throwable.getMessage());
                    }
                    
                    // Return cached user if available, even if expired
                    if (cached != null) {
                        System.out.println("Returning stale cached user for: " + userId);
                        return Optional.of(cached.user);
                    }
                    return Optional.<User>empty();
                } else {
                    // Cache the result
                    userCache.put(userId, new CachedUser(user, System.currentTimeMillis()));
                    return Optional.of(user);
                }
            });
    }
    
    private User fetchUserFromService(String userId) {
        try {
            // Simulate user service call with variable delay
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 2000 + 500)); // 0.5-2.5 seconds
            
            if (Math.random() < 0.2) { // 20% failure rate
                throw new UserServiceException("User service temporarily unavailable");
            }
            
            if (userId.equals("INVALID_USER")) {
                throw new UserNotFoundException("User not found: " + userId);
            }
            
            return new User(userId, "user" + userId + "@example.com", "User " + userId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UserServiceException("User service call interrupted", e);
        }
    }
    
    public void invalidateCache(String userId) {
        userCache.remove(userId);
    }
    
    public void clearCache() {
        userCache.clear();
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private static class CachedUser {
        final User user;
        final long timestamp;
        
        CachedUser(User user, long timestamp) {
            this.user = user;
            this.timestamp = timestamp;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }
}
