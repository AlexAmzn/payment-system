# Sample Codebase for MCP Server Testing

This is a generated codebase with known resilience patterns for testing the MCP server's ability to detect:
- Service dependencies
- Client interactions
- Retry mechanisms
- Circuit breakers
- Timeout configurations

## Services
- **OrderService**: Main business logic with multiple client dependencies
- **PaymentService**: External payment processing with retry logic
- **InventoryService**: Stock management with circuit breaker
- **NotificationService**: Email/SMS with timeout handling
- **UserService**: Authentication with caching

## Patterns to Detect
- Retry with exponential backoff
- Circuit breaker implementations
- Timeout configurations
- Service-to-service calls
- Error handling patterns
