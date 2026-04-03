# Custom Rate Limiter

A high-performance, distributed rate limiting service built with Spring Boot and Redis. This project implements multiple rate limiting algorithms to control the rate of requests and prevent abuse or overuse of resources.

## Features

- **Multiple Algorithms**: Supports Fixed Window, Sliding Window Counter, Sliding Window Log, and Token Bucket rate limiting strategies
- **Distributed**: Uses Redis for storage, enabling horizontal scaling across multiple instances
- **Spring Boot Integration**: Easy to integrate into existing Spring applications
- **Configurable Limits**: Set maximum requests per time window (default: 100 requests per 10 minutes)
- **IP-based Limiting**: Rate limits based on client IP address
- **Lua Scripts**: Efficient Redis operations using Lua scripts for atomicity

## Supported Algorithms

1. **Fixed Window**: Divides time into fixed intervals and counts requests within each window
2. **Sliding Window Counter**: Uses multiple fixed windows to provide smoother rate limiting
3. **Sliding Window Log**: Maintains a log of request timestamps within the window
4. **Token Bucket**: Tokens are added to a bucket at a fixed rate, requests consume tokens

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis Server (running on localhost:6379)

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd My-Rate-Limiter/rate-limiter
   ```

2. **Start Redis Server**
   Make sure Redis is running on localhost:6379 (default configuration)

3. **Build the application**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Usage

### Rate Limit Test Endpoint

```
GET /test-rate-limit?type={algorithm}
```

**Parameters:**
- `type`: The rate limiting algorithm to use. Supported values:
  - `fixed_window`
  - `sliding_window_counter`
  - `sliding_window_log`
  - `token_bucket`

**Response Codes:**
- `200 OK`: Request allowed
- `400 Bad Request`: Invalid algorithm type
- `429 Too Many Requests`: Rate limit exceeded
- `503 Service Unavailable`: Internal error

**Example:**
```bash
curl "http://localhost:8080/test-rate-limit?type=token_bucket"
```

## Configuration

The application can be configured via `application.properties`:

```properties
spring.application.name=rate-limiter
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### Rate Limits

Current configuration allows **100 requests per 10 minutes** per IP address for all algorithms. This can be modified by changing the `MAX_REQUESTS_ALLOWED_PER_10_MINUTES` constant in the `RateLimiter` interface.

## Testing

A test script is provided to simulate multiple requests:

```bash
# Test with 110 requests (exceeds the 100 limit)
for i in {1..110}; do
  curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8080/test-rate-limit?type=fixed_window"
done
```

Expected output: 100 `200` responses followed by 10 `429` responses.

## Architecture

- **Controller**: `RateLimitController` - Handles API requests
- **Filter**: `RateLimitingFilter` - Intercepts requests and applies rate limiting
- **Strategy**: `RateLimitingStrategy` - Selects the appropriate algorithm
- **Implementations**: Various `RateLimiter` implementations for different algorithms
- **Redis Config**: `RedisConfig` - Configures Redis connection and templates

## Redis Lua Scripts

The application uses Lua scripts for atomic Redis operations:
- `token-window-script.lua` - Token bucket algorithm
- `sliding-window-log-script.lua` - Sliding window log algorithm

## Development

### Project Structure
```
rate-limiter/
├── src/main/java/dev/adi/customimpl/ratelimiter/
│   ├── RateLimiterApplication.java
│   ├── RateLimiter.java
│   ├── configuration/
│   │   └── RedisConfig.java
│   ├── controller/
│   │   └── RateLimitController.java
│   ├── enums/
│   │   └── RateLimitingAlgorithm.java
│   ├── filter/
│   │   └── RateLimitingFilter.java
│   ├── impl/
│   │   ├── FixedWindowRateLimiter.java
│   │   ├── SlidingWindowCounter.java
│   │   ├── SlidingWindowLogRateLimiter.java
│   │   └── TokenBucketRateLimiter.java
│   └── strategy/
│       └── RateLimitingStrategy.java
├── src/main/resources/
│   ├── application.properties
│   └── lua-scripts/
│       ├── sliding-window-log-script.lua
│       └── token-window-script.lua
└── src/test/java/
    └── dev/adi/customimpl/ratelimiter/
        └── RateLimiterApplicationTests.java
```

### Adding New Algorithms

1. Add new enum value in `RateLimitingAlgorithm`
2. Implement `RateLimiter` interface
3. Add `@Service` annotation to the implementation
4. Create corresponding Lua script if needed

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request
