# FLOW — Run First

FLOW is intentionally **data-driven**. A fresh installation does not create demo users, vehicles or trips automatically.

## 1. Start PostgreSQL

Use your existing PostgreSQL installation or Docker. Create database `flow`.

## 2. Create the first administrator

Set these environment variables before starting the backend:

```powershell
$env:FLOW_BOOTSTRAP_ADMIN_USERNAME="your-admin"
$env:FLOW_BOOTSTRAP_ADMIN_PASSWORD="your-strong-password"
```

The bootstrap account is created only when that username does not exist. It is not sample data.

## 3. Start the backend

```powershell
cd FLOW-FINAL-RELEASE\backend\flow-backend
.\gradlew.bat bootRun
```

Verify:

```text
http://localhost:8080/api/health
```

The web operations console is:

```text
http://localhost:8080/
```

## 4. Create real operational data

Sign in as the administrator and create the people who will actually use the system:

1. Create Fleet Manager, Dispatcher and Driver accounts.
2. Create fleet vehicles from the backend/API or your operational management flow.
3. Sign in as Fleet Manager and create a trip using an enabled driver and available vehicle.
4. Dispatcher can review/reassign the trip.
5. Driver receives the assignment in Android, starts it, transmits GPS, and completes it.

## 5. Android emulator

Open `FLOW-FINAL-RELEASE/android` in Android Studio and run the `app` configuration on an API 37 emulator.

The development API URL is:

```text
http://10.0.2.2:8080/api/
```

Allow the local-network permission when Android requests it.

## 6. Production

Use `SPRING_PROFILES_ACTIVE=prod`, HTTPS, PostgreSQL, Flyway migrations and a strong externally supplied `JWT_SECRET`. Demo seeding is disabled.
