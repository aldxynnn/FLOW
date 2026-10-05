# FLOW — Fleet & Operations Platform

FLOW is a role-based fleet operations platform for managing vehicle trips from planning to completion.

The main operational flow is:

**plan → assign → execute → track → complete**

FLOW is designed as an internal operations system rather than a consumer-facing application. Different roles have different responsibilities and access within the same platform.

## What FLOW Does

FLOW connects fleet operations across Android, web, and backend services.

The platform currently supports:

* User authentication with role-based access
* Fleet and vehicle management
* Trip creation and assignment
* Driver trip execution
* Trip status management
* Real-time GPS location updates
* Live trip monitoring
* Trip location history
* WebSocket-based realtime events
* Offline-first driver actions
* Local action synchronization when connectivity returns
* Role-specific Android screens
* Web operations console
* PostgreSQL persistence

The system is designed around real operational data rather than hardcoded demo content.

## Roles

### ADMIN

Manages users and system-level administration.

Current responsibilities include:

* User management
* Role management
* Account enable/disable
* System overview

### FLEET_MANAGER

Manages fleet operations and trip planning.

Current responsibilities include:

* Viewing fleet information
* Creating trips
* Assigning drivers
* Monitoring trip status

### DISPATCHER

Supports day-to-day fleet operations.

Current responsibilities include:

* Viewing operational trips
* Monitoring assignments
* Monitoring active trips and GPS activity

### DRIVER

Executes assigned trips from the Android application.

Current responsibilities include:

* Viewing assigned trips
* Starting trips
* Completing trips
* Sending GPS location updates
* Continuing supported actions when offline
* Synchronizing queued actions when connectivity returns

## Platform Architecture

```text
                    ┌─────────────────────┐
                    │     PostgreSQL      │
                    │      Database       │
                    └──────────▲──────────┘
                               │
                    ┌──────────┴──────────┐
                    │   Spring Boot API   │
                    │       Backend       │
                    │                     │
                    │ Kotlin              │
                    │ Spring Boot         │
                    │ Spring Security     │
                    │ JWT                 │
                    │ Spring Data JPA     │
                    │ WebSocket           │
                    └───────▲───────▲─────┘
                            │       │
                   REST / WS│       │REST / WS
                            │       │
             ┌──────────────┘       └──────────────┐
             │                                     │
   ┌─────────┴─────────┐                 ┌─────────┴─────────┐
   │   Android App     │                 │   Web Console     │
   │                   │                 │                   │
   │ Kotlin            │                 │ HTML              │
   │ Jetpack Compose   │                 │ CSS               │
   │ Retrofit          │                 │ JavaScript        │
   │ OkHttp            │                 │ WebSocket          │
   │ DataStore         │                 │ OpenStreetMap + MapLibre JS     │
   │ WorkManager       │                 │                   │
   │ GPS Location      │                 │                   │
   └───────────────────┘                 └───────────────────┘
```

## Repository Structure

```text
FLOW/
├── android/
│   └── app/
│       └── src/
│
├── backend/
│   └── flow-backend/
│       └── src/
│           ├── main/
│           └── test/
│
├── web/
│   ├── index.html
│   ├── app.js
│   └── styles.css
│
├── docs/
│
└── .github/
    └── workflows/
        └── ci.yml
```

## Android Application

The Android application is built with:

* Kotlin
* Jetpack Compose
* Material 3
* Navigation Compose
* ViewModel
* Kotlin Coroutines / Flow
* Retrofit
* OkHttp
* DataStore
* WorkManager
* Google Play Services Location
* OpenStreetMap + MapLibre map renderer in the Android tracking WebView

The Android application is used primarily for operational workflows, especially driver-side trip execution and GPS tracking.

The current Android tracking map uses OpenStreetMap + MapLibre map renderer in the Android tracking WebView with the standard roadmap renderer.

## Backend

The backend is built with:

* Kotlin
* Spring Boot
* Spring Security
* JWT authentication
* Spring Data JPA
* PostgreSQL
* WebSocket
* Gradle

The backend provides authenticated APIs for:

* Authentication
* Users
* Vehicles
* Trips
* Trip locations
* Dashboard/summary data
* Realtime operational events

The backend is the central source of truth for operational data.

## Realtime Tracking

FLOW supports realtime trip updates.

When a driver sends a GPS location update:

1. The Android application obtains the driver's current location.
2. The location is sent to the backend.
3. The backend stores the location.
4. The backend broadcasts a realtime event through WebSocket.
5. Connected clients can update the operational view.

Trip events such as starting, completing, assigning, creating, and location updates are supported by the realtime event system.

## Offline-First Driver Workflow

The driver workflow supports operation when network connectivity is temporarily unavailable.

Supported actions can be queued locally when the backend cannot be reached.

The synchronization flow is:

```text
Driver action
     ↓
Backend available?
   /       \
 YES       NO
  ↓         ↓
API call   Local queue
  ↓         ↓
Update     WorkManager
server     retry
             ↓
        Network returns
             ↓
          Sync API
```

The current implementation specifically supports offline queuing for supported trip actions such as:

* START
* COMPLETE

GPS/location synchronization is also part of the driver tracking architecture.

## Authentication & Authorization

FLOW uses authenticated access with role-based authorization.

Each authenticated user has a role:

```text
ADMIN
FLEET_MANAGER
DISPATCHER
DRIVER
```

The backend controls access to protected operations, while the Android and web applications provide role-specific interfaces.

## Data & Persistence

Operational data is persisted in PostgreSQL.

The system currently stores data such as:

* Users
* Vehicles
* Trips
* Trip status
* Driver assignments
* Trip timestamps
* Current GPS coordinates
* Trip location history

There is no requirement for hardcoded demo trips or vehicles for the application to operate.

For a fresh environment, an administrator can be bootstrapped through environment variables:

```text
FLOW_BOOTSTRAP_ADMIN_USERNAME
FLOW_BOOTSTRAP_ADMIN_PASSWORD
```

The normal application flow is based on authenticated runtime data.

## Running Locally

### 1. PostgreSQL

Create a PostgreSQL database:

```text
flow
```

Make sure PostgreSQL is running locally.

### 2. Backend

Open:

```text
backend/flow-backend
```

Then run:

```powershell
.\gradlew.bat bootRun
```

The backend runs on:

```text
http://localhost:8080
```

API base URL:

```text
http://localhost:8080/api/
```

### 3. Bootstrap Administrator

For a fresh environment, configure:

```powershell
$env:FLOW_BOOTSTRAP_ADMIN_USERNAME="admin"
$env:FLOW_BOOTSTRAP_ADMIN_PASSWORD="REPLACE_WITH_A_STRONG_PASSWORD!"
```

Then start the backend:

```powershell
.\gradlew.bat bootRun
```

Use a stronger password for any non-local environment.

### 4. Web Console

Open:

```text
web/
```

Run a local static server:

```powershell
python -m http.server 5500
```

Then open:

```text
http://localhost:5500/
```

The web console communicates with:

```text
http://localhost:8080/api/
```

### 5. Android

Open:

```text
android/
```

in Android Studio.

For the Android emulator, the default development API endpoint is:

```text
http://10.0.2.2:8080/api/
```

The Android application requires the backend to be running.

## Verification

The current checkpoint has been verified with the following build checks.

### Android

```powershell
.\gradlew.bat assembleDebug
```

Result:

```text
BUILD SUCCESSFUL
```

### Android Signing

```powershell
.\gradlew.bat signingReport
```

Result:

```text
BUILD SUCCESSFUL
```

### Backend

```powershell
.\gradlew.bat test
```

Result:

```text
BUILD SUCCESSFUL
```

The application has also been manually tested across the Android application, backend, and web console during development.

## Current Status

FLOW is currently at a functional development checkpoint.

The core operational flow is implemented:

```text
Login
  ↓
Role-based access
  ↓
Fleet / trip management
  ↓
Driver assignment
  ↓
Trip start
  ↓
GPS tracking
  ↓
Realtime updates
  ↓
Trip completion
```

The project is intentionally still open for further development.

Potential future improvements include:

* More advanced map visualization
* Map-based trip creation
* Route and ETA calculation
* More detailed analytics
* Expanded notification handling
* Additional automated tests
* Production deployment
* Monitoring and observability
* Further UI/UX refinement

These are future improvements and are not presented as completed features of the current checkpoint.

## Project Goal

FLOW was built as a practical full-stack fleet operations project combining:

* Android development
* Backend API development
* Authentication and authorization
* Database persistence
* GPS/location services
* Realtime communication
* Offline-first application design
* Web operations tooling
* CI and automated build verification

The project demonstrates how a mobile application, backend service, database, realtime communication, and operational web interface can work together as one product.
