# Contributing to log-forwarder-atc

Thank you for your interest in contributing. This document covers how to build, test, and submit changes.

## Prerequisites

- JDK 21
- Docker (for integration tests and local database)
- Git

Maven is optional; the repo includes a Maven Wrapper (`./mvnw`).

## Getting started

```bash
git clone https://github.com/sanjuthomas/log-forwarder-atc.git
cd log-forwarder-atc
docker compose up -d timescaledb
./mvnw spring-boot:run
```

Open http://localhost:8090/ for the fleet dashboard.

## Building and testing

```bash
./mvnw verify
```

This runs unit tests, integration tests (Testcontainers + TimescaleDB), and enforces **80%** line coverage via JaCoCo.

Run only unit tests:

```bash
./mvnw test
```

## Pull requests

1. Fork the repository and create a feature branch from `main`.
2. Make focused changes with tests where behavior changes.
3. Run `./mvnw verify` locally before opening a PR.
4. Open a pull request with a clear description of the change and how you tested it.

Please keep PRs small and scoped to one concern when possible.

## Code style

- Match existing patterns in the codebase (Spring Boot services, records for DTOs, Flyway for schema changes).
- Use meaningful test names and cover new behavior.
- Do not commit secrets, credentials, or `.env` files.

## Reporting issues

Use [GitHub Issues](https://github.com/sanjuthomas/log-forwarder-atc/issues) for bugs and feature requests. For security vulnerabilities, see [SECURITY.md](SECURITY.md).

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).
