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
