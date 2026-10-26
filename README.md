# Spring Boot Logging Demo

A small REST API that demonstrates production-style logging with **SLF4J + Logback** in Spring Boot:
log levels, console and file output, a per-request id, and centralized exception logging.

## Tech stack

- Java 17+
- Spring Boot 3.5.x (`spring-boot-starter-web` only; it already includes SLF4J and Logback)
- Maven

## Run it

```bash
mvn spring-boot:run
```

Or build a jar and run it:

```bash
mvn clean package
java -jar target/logging-demo-1.0.0.jar
```

The API starts on `http://localhost:8080`. Run tests with `mvn test`.

> Start the app from the project root so the log file lands in `./logs/application.log`.

## Project structure

```
src/main/java/com/example/loggingdemo
├── LoggingDemoApplication.java      Entry point (logs startup)
├── controller
│   ├── ProductController.java       CRUD-style REST endpoints
│   └── DemoController.java          Endpoints that exist only to show log levels / errors
├── service/ProductService.java      Business logic, in-memory storage
├── model/Product.java               Domain model
├── exception
│   ├── GlobalExceptionHandler.java  @ControllerAdvice: exception -> JSON + log
│   ├── ProductNotFoundException.java
│   ├── InvalidProductException.java
│   └── ErrorResponse.java           JSON error body
└── config/RequestLoggingFilter.java Request id (MDC) + request summary line
src/main/resources/application.properties   All logging configuration
```

## API examples

```bash
# List products (two are seeded at startup)
curl -i http://localhost:8080/api/products

# Get one product
curl -i http://localhost:8080/api/products/1

# Create a product
curl -i -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Monitor","price":199.99,"stock":10}'

# Reduce stock (logs WARN once stock drops below 5)
curl -i -X PATCH "http://localhost:8080/api/products/1/stock?delta=-22"

# Delete a product
curl -i -X DELETE http://localhost:8080/api/products/2

# Show every log level at once
curl http://localhost:8080/api/demo/log-levels

# Trigger an unexpected exception (ERROR with stack trace)
curl -i http://localhost:8080/api/demo/error
```

Error cases:

```bash
curl -i http://localhost:8080/api/products/999                       # 404, WARN
curl -i -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" -d '{"name":"","price":5,"stock":1}'   # 400, WARN
curl -i -X PATCH "http://localhost:8080/api/products/1/stock?delta=-500"     # 400, WARN
curl -i http://localhost:8080/api/products/abc                       # 400, WARN
```

Every response includes an `X-Request-Id` header. Search the logs for that value to see every line
produced by that request.

## How logging is configured

Everything lives in `src/main/resources/application.properties`; no `logback-spring.xml` is needed.

| Property | Purpose |
|---|---|
| `logging.level.root=INFO` | Default threshold for all libraries |
| `logging.level.com.example.loggingdemo=DEBUG` | Our code logs DEBUG and above |
| `logging.level.org.springframework.web=INFO` | Keeps Spring MVC quiet; raise to DEBUG to investigate |
| `logging.file.name=logs/application.log` | Turns on file logging, in addition to the console |
| `logging.logback.rollingpolicy.*` | Roll at 10MB, keep 7 days, cap archives at 100MB |
| `logging.pattern.console` / `logging.pattern.file` | Output formats, including `%X{requestId}` |

Levels from least to most severe: `TRACE < DEBUG < INFO < WARN < ERROR`. A logger prints a message only if
the message's level is at or above its configured level. With the settings above, `TRACE` is hidden for our
code, and `DEBUG` is hidden for everything else.

### Changing levels without editing the file

```bash
# Environment variable
LOGGING_LEVEL_COM_EXAMPLE_LOGGINGDEMO=INFO mvn spring-boot:run

# Command-line argument
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.example.loggingdemo=TRACE"
```

## What each level is used for here

| Level | Where | Example |
|---|---|---|
| `INFO` | Controllers, service, filter | `Received request to get product id=1`, `Created product id=3`, `<-- GET /api/products status=200 duration=4ms` |
| `DEBUG` | Service, filter | `Looking up product id=1`, `Validating new product: ...`, `--> GET /api/products` |
| `WARN` | Service, `GlobalExceptionHandler` | `Low stock for product ...`, `Resource not found: ...`, `Invalid request rejected: ...` |
| `ERROR` | `GlobalExceptionHandler` | `Unexpected error while processing GET /api/demo/error` plus a stack trace |

Design choices worth noting:

- **Client mistakes are WARN, not ERROR.** A 404 or 400 is the client's problem; logging them as ERROR would bury real failures.
- **Only unexpected exceptions get a stack trace**, and only in one place (`GlobalExceptionHandler`), so each failure is logged once.
- **Parameterized messages** (`log.info("id={}", id)`) avoid string concatenation when the level is disabled.
- **The request id** is stored in the MDC by `RequestLoggingFilter` and cleared in a `finally` block, because server threads are reused.

## Verify logging locally

1. Start the app and watch the console: you should see startup `INFO` lines and `Seeded 2 sample products`.
2. Run `curl http://localhost:8080/api/demo/log-levels`. The console and file show DEBUG, INFO, WARN and ERROR, but **not** TRACE.
3. Run `tail -f logs/application.log` in a second terminal and call other endpoints.
4. Trigger `GET /api/demo/error` and confirm an `ERROR` line followed by a stack trace in the file.
5. Take the `X-Request-Id` from a response (`curl -i ...`) and run `grep <id> logs/application.log`.
6. Change `logging.level.com.example.loggingdemo` to `INFO` and restart: DEBUG lines disappear. Set it to `TRACE`: the TRACE line from `/api/demo/log-levels` appears.

Sample output (file):

```
2026-10-26 23:30:12.101 INFO  [http-nio-8080-exec-1] [a1b2c3d4] c.e.l.controller.ProductController - Received request to get product id=999
2026-10-26 23:30:12.103 WARN  [http-nio-8080-exec-1] [a1b2c3d4] c.e.l.exception.GlobalExceptionHandler - Resource not found: Product with id 999 not found (path=/api/products/999)
2026-10-26 23:30:12.105 INFO  [http-nio-8080-exec-1] [a1b2c3d4] c.e.l.config.RequestLoggingFilter - <-- GET /api/products/999 status=404 duration=5ms
```
