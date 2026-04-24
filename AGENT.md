# Agent Context — canalradionov-service

## What this app is

Backend API for Canal Radionov, a radio/broadcast streaming platform. Handles auth, broadcast management, and streaming metadata.

## Stack

Java 17, Spring Boot 3.3, Spring Security, OAuth2 Resource Server (JWT), springdoc/Swagger, Maven

## Project structure

- Standard Spring Boot layout: controllers, services, repositories, entities, config
- OAuth2 resource server — JWT tokens validated against an auth server
- Swagger/OpenAPI docs auto-generated

## Key patterns

- REST API consumed by canalradionov-ui frontend
- JWT-based auth via Spring Security OAuth2
- Swagger annotations on controllers for API documentation

## Focus for this agent

- Controller endpoints that are stubbed or return empty/hardcoded responses
- Service methods declared but not implemented
- Missing validation or error handling on endpoints
- Any broadcast/streaming-related features partially built
