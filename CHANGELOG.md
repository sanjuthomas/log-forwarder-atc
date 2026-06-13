# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- MIT license and open-source project metadata
- Maven Wrapper, Dockerfile, and full-stack `docker compose` setup
- OpenAPI documentation (Swagger UI)
- Testcontainers integration test against TimescaleDB
- Dependabot, release workflow, and JaCoCo coverage reporting in CI
- Consistent JSON error responses via global exception handler
- Contributing, security, and code of conduct documentation

## [0.1.0] - 2026-06-13

### Added

- Agent registration and deregistration (`PUT` / `DELETE` `/api/instances`)
- Scheduled polling of agent `/health`, `/ready`, and `/metrics` endpoints
- PostgreSQL + TimescaleDB storage with Flyway migrations
- Fleet dashboard with SSE live updates
- REST API for instance status, metrics history, and deregistration history
- GitHub Actions CI with JaCoCo coverage enforcement (80% minimum)

[Unreleased]: https://github.com/sanjuthomas/log-forwarder-atc/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/sanjuthomas/log-forwarder-atc/releases/tag/v0.1.0
