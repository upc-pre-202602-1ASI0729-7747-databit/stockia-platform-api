# Changelog

All notable changes to the StockIA Platform API are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - YYYY-MM-DD

### Added
- RESTful API with Spring Boot 3.5, Spring Data JPA and MySQL, organized by the eight bounded contexts of the
  C4 model plus `shared`: IAM, Restaurant Registration, Subscription & Payments, Stock Management,
  Recipes Management, Notifications & Messaging, ML & Recommendations and Analytics & Dashboard.
- JWT authentication with Spring Security, Administrator and Employee roles, account lock after five failed
  sign-ins and password recovery with a six-digit code.
- Supply lots with shelf life, sales with automatic ingredient deduction (freshest usable lot first) and voids.
- Automatic inventory alerts with simulated WhatsApp and email delivery.
- Seven-day demand forecast with an ONNX machine learning model, the Peruvian calendar and the Open-Meteo weather
  service, plus purchase recommendations.
- Dashboard KPIs and weekly savings.
- OpenAPI documentation with Swagger UI, English and Latin American Spanish messages (`Accept-Language`).
- Unit tests, integration test, smoke test against MySQL and GitHub Actions workflow.
- Dockerfile and docker-compose for local and cloud deployment.