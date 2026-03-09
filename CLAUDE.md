# HTTPLoadTest-Baseline

## Purpose
A simple, extensible starting point for building HTTP load tests designed for continuous deployment and continuous production QA pipelines. Built on Hystrix circuit-breaker framework to avoid blocking or dangling HTTP requests. Intended to be forked and customized for specific load testing needs.

## Tech Stack
- Language: Java 8+
- Framework: Jersey 2.x, Jetty 9.x, Spring 5.x
- Build: Maven
- Key dependencies: Hystrix, Jersey, Jetty, Spring
- Docker: Available as `cantara/httploadtest-baseline`

## Architecture
Standalone web application with embedded Jetty server. Provides a web UI for configuring and running load tests, REST APIs for programmatic control, and built-in result aggregation with percentile-based benchmarking. Uses Hystrix commands for resilient HTTP execution with circuit-breaker patterns. Designed to be embedded into CI/CD pipelines for automated non-functional QA.

## Key Entry Points
- Web UI: `http://localhost:28086/HTTPLoadTest-baseline/config`
- Health: `http://localhost:28086/HTTPLoadTest-baseline/health`
- Run status: `http://localhost:28086/HTTPLoadTest-baseline/loadTest/runstatus`
- `doc/tutorial/index.md` - Getting started tutorial

## Development
```bash
# Build
mvn clean install

# Run locally
java -jar target/HTTPLoadTest-baseline-*.jar

# Run via Docker
docker run -d -p 28086:8086 cantara/httploadtest-baseline
```

## Domain Context
Performance and load testing infrastructure. Provides non-functional QA capabilities that can be embedded into CI/CD pipelines for continuous performance validation of microservices.
