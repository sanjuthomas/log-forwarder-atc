# AGENTS.md

Guidance for AI coding agents working in **log-forwarder-atc**.

## Project summary

Spring Boot service that registers **log-forwarder** agents, polls their `/health`, `/ready`, and `/metrics` endpoints, stores fleet state in PostgreSQL/TimescaleDB, and serves a fleet dashboard plus REST/SSE API.

Stack: Java **21**, Maven Wrapper (`./mvnw`), Flyway migrations, JaCoCo (**80% minimum overall coverage**).

---

## Test coverage policy (required)

**Minimum overall coverage: 80%** on the project bundle, enforced by JaCoCo during `./mvnw verify`.

Configured in `pom.xml` as `${jacoco.minimum.coverage}` (currently **0.80**). The build fails if **any** of these bundle ratios drop below 80%:

| Metric | Enforced |
|--------|----------|
| Instructions | Yes |
| Branches | Yes |
| Lines | Yes |

Agents **must**:

1. Run `./mvnw verify` after code changes — not `./mvnw test` alone (JaCoCo `check` runs in the `verify` phase).
2. Add or update tests when new behavior would drop coverage below 80%.
3. Not lower `jacoco.minimum.coverage` or remove JaCoCo limits without explicit maintainer approval.
4. Report local coverage from `target/site/jacoco/index.html` when debugging gaps.

---

## Spring Boot version policy (required)

**Authoritative version:** `spring-boot-starter-parent` in `pom.xml` (currently **4.1.0**).

Agents **must**:

1. Keep the project on **Spring Boot 4.x**. Do not downgrade to Boot 3 or mix Boot 3 APIs/starters.
2. Change the Boot version **only** by updating `<parent>` in `pom.xml`. Do not pin Spring Framework, Tomcat, or other Boot-managed artifacts separately unless Boot docs require it.
3. Use **Boot 4 modular starters** — not deprecated Boot 3 names:

   | Use (Boot 4) | Do not use (Boot 3 / deprecated) |
   |--------------|-------------------------------------|
   | `spring-boot-starter-webmvc` | `spring-boot-starter-web` |
   | `spring-boot-starter-webclient` | `spring-boot-starter-webflux` (for client-only WebClient) |
   | `spring-boot-starter-flyway` | raw `flyway-core` alone |
   | `spring-boot-starter-webmvc-test` | expecting `@WebMvcTest` from `starter-test` only |
   | `spring-boot-resttestclient` + `@AutoConfigureTestRestTemplate` | `org.springframework.boot.test.web.client.TestRestTemplate` without resttestclient |

4. Align **springdoc** with Boot 4: use **springdoc 3.x** (`${springdoc.version}` in `pom.xml`, currently **3.0.3**). Do not use springdoc 2.x on Boot 4.
5. When bumping Boot, run `./mvnw verify` and fix test autoconfigure / starter modularization before finishing.
6. Reject or defer Dependabot **major** bumps that target Boot 3-era stacks (e.g. springdoc 2.x only, Testcontainers 2.x without migration) unless explicitly requested.

**Compatible versions** (defined in `pom.xml` `<properties>` — keep in sync when upgrading):

| Property | Current | Notes |
|----------|---------|--------|
| `java.version` | 21 | Minimum for this repo |
| Parent Boot | 4.1.0 | Single source of truth |
| `springdoc.version` | 3.0.3 | Must match Boot 4 |
| `testcontainers.version` | 1.20.4 | Stay on 1.x until a dedicated TC 2 migration |
| `okhttp.version` | 5.4.0 | Required for `mockwebserver` (not Boot-managed) |
| `jacoco.minimum.coverage` | 0.80 | Minimum overall bundle coverage (80%) |

---

## Testing conventions (Spring Boot 4)

- Use `@MockitoBean` / `@MockitoSpyBean` from `org.springframework.test.context.bean.override.mockito` — **not** `@MockBean` / `@SpyBean`.
- `@WebMvcTest` import: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`.
- Full-stack tests with HTTP client: `@AutoConfigureTestRestTemplate` and `org.springframework.boot.resttestclient.TestRestTemplate`.
- Integration tests: `@SpringBootTest` + Testcontainers TimescaleDB (`LogForwarderAtcIntegrationTest` pattern).
- Always run `./mvnw verify` before proposing dependency or test changes; **overall coverage must stay ≥ 80%**.

---

## Code conventions

- Package root: `com.logforwarder.atc`
- DTOs: Java `record`s with Jakarta validation and Jackson `@JsonProperty` where JSON names differ.
- Persistence: JPA entities + Flyway SQL under `src/main/resources/db/migration/`.
- Errors: `ResponseStatusException` + `GlobalExceptionHandler` → JSON `ApiError`.
- Match existing style; avoid unrelated refactors.

---

## Commands

```bash
docker compose up -d timescaledb   # database only
./mvnw spring-boot:run             # run ATC on :8090
./mvnw verify                      # tests + JaCoCo gate
./mvnw clean package               # build JAR
```

Dashboard: http://localhost:8090/  
OpenAPI UI: http://localhost:8090/swagger-ui.html

---

## Do not

- Commit secrets, `.env`, or credentials.
- Add Spring Security or auth changes unless explicitly requested.
- Edit unrelated files or expand scope beyond the task.
- Introduce Boot 3 starters, `@MockBean`, or springdoc 2.x while on Boot 4.

---

## References

- [README.md](README.md) — API, agent contract, configuration
- [CONTRIBUTING.md](CONTRIBUTING.md) — PR workflow
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
