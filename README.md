canalradionov-service
Overview
This project is a Spring Boot service designed to act as an intermediary between a canalradionov-ui AWS Cognito. It validates and manages JSON Web Tokens (JWTs) for authentication and authorization purposes.

The main responsibilities of the service include:

Validating JWT tokens received from the front-end.
Interfacing with AWS Cognito for user authentication and token verification.
Providing endpoints that require valid JWTs for access.
Features
JWT Authentication: The service intercepts HTTP requests, validates JWTs, and allows or denies access to secure endpoints based on the validity and claims of the token.
AWS Cognito Integration: The service connects to AWS Cognito to verify the authenticity of tokens and retrieve user information.
Role-based Access Control: By reading the roles from JWT claims, it ensures that users have the necessary permissions to access specific resources.
Requirements
Java 17+
Maven 3+
AWS Cognito setup with a user pool and an application client
Spring Boot 3.x
