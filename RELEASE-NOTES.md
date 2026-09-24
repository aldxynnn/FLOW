# FLOW — Release Candidate UI & Operations Upgrade

This package is a source-controlled release candidate built from the FLOW project source.

## Included in this iteration

- ADMIN control center with People, Fleet, and Trips sections.
- ADMIN can create real users for DRIVER, DISPATCHER, FLEET_MANAGER, and ADMIN roles (subject to server-side admin policy).
- ADMIN can add vehicles and operational trips.
- FLEET_MANAGER can create trips and vehicles.
- DISPATCHER can assign/reassign drivers.
- DRIVER sees assigned trips and can start/complete trips.
- Clear status filtering for operational trips.
- More polished Material 3 visual system: stronger hierarchy, hero panels, metric cards, status chips, spacing, and role-specific navigation.
- Server error bodies are surfaced to Android instead of collapsing every API failure into a generic message.
- Fresh database remains free of demo data unless bootstrap admin environment variables are explicitly supplied.
- Bootstrap password encoding is explicitly null-safe.

## Verification status

The source was statically reviewed after the changes. Full Gradle verification was not completed in the packaging environment because the Gradle wrapper attempted to download Gradle 9.6.0 and outbound network access was unavailable.

Before pushing this package to GitHub, run the repository validation and local Android/backend builds described in `docs/RELEASE-CHECKLIST.md`.


## GPS map hardening

- Android foreground tracking prevents duplicate location callbacks when a trip is opened repeatedly.
- GPS writes validate coordinates and return structured results to the debug UI.
- Android debug builds include a release-hidden GPS simulator for deterministic end-to-end map verification.
- Web tracking uses the latest trip coordinate immediately, resizes the MapLibre canvas after render, fits multi-point trails to bounds, and surfaces tile/library failures instead of leaving a blank panel.

- Web tracking map now uses an inline MapLibre raster style backed by the standard OpenStreetMap raster endpoint instead of depending on an external OpenFreeMap style document.
- Web tracking keeps a visible current GPS point even when historical points have not yet loaded; the backend history endpoint also falls back to the trip's latest stored coordinate when history is empty.

## 2026-09-24 — Web live map renderer fix
- Replaced the web trip tracking renderer with Leaflet 1.9.4 for predictable raster-tile rendering.
- Primary tiles: OpenStreetMap standard tiles.
- Automatic fallback: CARTO Voyager tiles when the primary tile service fails.
- Preserved realtime GPS trail, current-position marker, point counter, and last-update metadata.
- Added a visible map-tile error state without hiding available GPS data.
