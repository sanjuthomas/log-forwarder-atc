# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Upgrade to Spring Boot 4.1, springdoc OpenAPI 3, and modular Boot 4 starters (webmvc, webclient, flyway)

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
