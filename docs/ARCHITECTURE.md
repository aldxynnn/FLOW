# Architecture

Android Driver App
    -> Retrofit HTTP API
    -> Spring Boot
    -> PostgreSQL

Active trip GPS:
Android foreground location service
    -> POST /api/trips/{id}/location

Offline status sync:
Android DataStore queue
    -> WorkManager with connected-network constraint
    -> REST API

Operations realtime:
Spring WebSocket endpoint `/ws/ops`
    -> broadcast trip events
    -> Web dashboard
