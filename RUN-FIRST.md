# FLOW — Run First

FLOW is intentionally **data-driven**. A fresh installation does not create demo vehicles or trips automatically.

## Fastest local setup on Windows

Prerequisites:
- Docker Desktop
- Android Studio with the Android SDK
- Java 21
- Python 3

After cloning the repository, run:

```powershell
.\scripts\dev.bat
```

That launcher automatically:
- starts PostgreSQL for FLOW on port `5433` so it does not require port `5432`
- configures the local backend connection
- configures a local development JWT secret
- creates the first `admin` account on a fresh database
- starts the backend on `http://localhost:8080`
- starts the web console on `http://localhost:5500`

Local development login:

```text
Username: admin
Password: TestPassword-ChangeMe-123!
```

The password above is **development-only**. Change it for any non-local environment.

## Android

Open the `android/` directory in Android Studio and run the `app` configuration on an API 37 emulator.

The Android emulator uses:

```text
http://10.0.2.2:8080/api/
```

The backend must be running first. After `scripts\dev.bat` reports `FLOW READY`, Android can be launched normally.

## Manual backend startup

If you do not want to use the launcher, the backend can still be started directly from `backend/flow-backend`, but the required database and authentication environment variables must be configured first.

## Production

Use `SPRING_PROFILES_ACTIVE=prod`, HTTPS, PostgreSQL, Flyway migrations and a strong externally supplied `JWT_SECRET`. Do not reuse the local development credentials.

## Deterministic GPS map test

Android debug builds expose a small `DEBUG GPS` panel inside an `IN_PROGRESS` driver trip. It sends test points through the same `/api/trips/{id}/location` endpoint used by the real foreground location service.

The panel is compiled behind `BuildConfig.DEBUG` and is not shown in release builds.

## OpenStreetMap + MapLibre maps

No Google Maps API key or Google Cloud billing is needed for this portfolio build. The Web tracking map and Android tracking map use MapLibre with OpenStreetMap Shortbread vector tiles. Internet access is required while the map is visible.