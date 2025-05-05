# Spring Cloud Contract Demo

A demonstration repository showing contract-driven development between microservices using Spring Cloud Contract.

## Overview

This repository contains two Spring Boot services that communicate via HTTP, with contract testing ensuring API compatibility:

- **Producer**: REST API serving boardgame data for years 2010-2025
- **Consumer**: HTTP client consuming the producer's API using declarative interfaces

## Architecture

### Producer Service
- Single endpoint: `GET /boardgame/{year}`
- Returns best boardgame for given year with title, publication year, and average rating
- Validates year range (2010-2025) with proper error responses
- Uses method-level validation with custom exception handling

### Consumer Service  
- Declarative HTTP client using Spring's `@HttpExchange`
- Custom error handling for 4xx responses via `BoardgameClientException`
- Configuration supports both real service and stub runner for testing

## Contract Testing Flow

1. **Producer** defines API contracts in YAML files under `src/contractTest/resources/contracts/`
2. Spring Cloud Contract plugin generates verification tests from contracts
3. Producer tests validate actual implementation matches contracts
4. Stubs are generated and published to Maven local repository
5. **Consumer** uses `@AutoConfigureStubRunner` to test against generated stubs
6. Consumer tests validate client behavior without requiring running producer

## Key Files

### Producer Contracts
- `duneImperiumResponse.yml` - Success case contract
- `badRequestResponse.yml` - Error case contract

### Generated Artifacts
- Contract verification tests (auto-generated)
- JAR with stubs published to Maven local

### Consumer Tests
- `BoardgameClientContractTest` - Tests both success and error scenarios using stubs

## Benefits Demonstrated

- **Decoupled Development**: Services can develop and test independently
- **API Compatibility**: Contract changes break builds, preventing integration issues  
- **Automated Testing**: Contract compliance verified on both producer and consumer sides
- **Fast Feedback**: No need to spin up dependent services for testing

## Tech Stack

- Spring Boot 3.4.3
- Spring Cloud Contract 4.2.0
- Java 23
- Gradle 8.12.1
- JUnit 5

## Reference

- [Spring Cloud Contract Documentation](https://docs.spring.io/spring-cloud-contract/docs/current/reference/html/getting-started.html)