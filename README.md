# FLOW — Fleet & Operations Platform

FLOW is a role-based fleet operations platform for the complete operational loop:

**plan → assign → execute → track → complete**

The application is deliberately data-driven. It does not require demo trips, demo vehicles or demo accounts to make the UI look populated.

## Roles

- **ADMIN** — manages user access and system-level administration.
- **FLEET_MANAGER** — plans trips and works with the available fleet.
- **DISPATCHER** — controls assignments and monitors live operations.
- **DRIVER** — receives assigned work, starts/completes trips, and sends GPS updates with offline-first support.

The same product has different responsibilities by role; users do not simply see the same dashboard with different labels.

## Repository layout

- `android/` — Kotlin + Jetpack Compose mobile application
- `backend/flow-backend/` — Kotlin + Spring Boot API, PostgreSQL, JWT and WebSocket
- `web/` — operations console source
- `docs/` — architecture, product and release documentation
- `.github/workflows/ci.yml` — CI workflow

## Data model

There is no required hardcoded demo dataset. Runtime records come from PostgreSQL and are created through authenticated operational flows.

For a fresh environment, bootstrap one administrator using:

```text
FLOW_BOOTSTRAP_ADMIN_USERNAME
FLOW_BOOTSTRAP_ADMIN_PASSWORD
```

`FLOW_SEED_ENABLED` defaults to `false`. The old demo credentials are no longer part of the normal setup.

## Run locally

1. Start PostgreSQL and create database `flow`.
2. Configure database/JWT environment variables.
3. Start the backend from `backend/flow-backend`.
4. Open `http://localhost:8080/` for the web console.
5. Open `android/` in Android Studio for the mobile app.

For the Android emulator, the default development endpoint is `http://10.0.2.2:8080/api/`.

## Production

Use `SPRING_PROFILES_ACTIVE=prod`, HTTPS, PostgreSQL, Flyway and an externally supplied JWT secret. Production disables demo seeding and validates the schema.

## Offline-first driver workflow

Driver `START`, `COMPLETE` and GPS samples can be queued locally when connectivity is unavailable. WorkManager retries synchronization when the network returns. Local data is bounded rather than pretending to be a second source of truth.

## Release verification

A release is only considered complete after runtime verification of:

- backend tests
- Android build
- login for every role
- role-specific authorization and UI
- trip create/assign/start/complete lifecycle
- GPS updates
- offline queue + reconnect synchronization
- WebSocket refresh/reconnect
- production configuration and migrations

The archive contains source changes; runtime verification still depends on the target machine, Android emulator and PostgreSQL instance.
