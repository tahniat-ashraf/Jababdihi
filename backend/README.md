# Backend

Backend workspace for Jababdihi.

## Stack

- Java 25
- Spring Boot 3.5
- Spring Security
- Spring AI integration where useful
- PostgreSQL with pgvector
- Redis
- Quartz Scheduler
- Flyway versioned SQL migrations

## Runtime Shape

The backend will use one codebase with separate runtime processes/profiles:

- `api`: public API and admin/internal API.
- `worker`: ingestion, AI processing, deduplication, scheduled jobs.

## MVP Responsibilities

- Public read-only incident and category APIs.
- Internal admin APIs protected by Basic Auth for MVP.
- Machine-to-machine internal APIs protected by API key.
- PostgreSQL-backed `processing_tasks` durable task queue.
- Quartz jobs backed by PostgreSQL job state.
- RSS-first ingestion with direct scraping fallback per allowlisted publisher.
- AI-assisted extraction, summarization, translation, and deduplication through private services only.

## Spring Profiles

- `api`: public API and admin/internal API.
- `worker`: ingestion, AI processing, deduplication, scheduled jobs.

The `api` profile is the default local profile.

## Local Setup

Run tests:

```bash
mvn test
```

Run the API profile locally:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=api
```

The health endpoint is available at:

```text
GET /actuator/health
```

Build the application:

```bash
mvn package
```

Database, Redis, Flyway, and Quartz dependencies are present but their runtime auto-configuration is disabled until local infrastructure configuration is added.

## Boundaries

- Public APIs must be read-only.
- PostgreSQL, Redis, Ollama, and worker services must not be publicly exposed.
- Use Flyway forward-fix migrations; do not rely on destructive rollbacks.
- Do not publish incidents unless they meet the documented auto-publish criteria or are manually published.
