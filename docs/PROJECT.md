# FLOW Project Definition

FLOW is a fleet and operations platform connecting drivers, dispatchers, fleet managers and administrators around trips, vehicles, GPS status and operational analytics.

Primary workflow:

Manager creates/assigns trip -> Driver receives assignment -> Driver starts trip -> GPS updates location -> Driver completes trip -> Dashboard metrics update.

Core product rules:
- Driver app is action-first and one-handed.
- Operations dashboard is data-first.
- Offline trip status actions must survive temporary connectivity loss.
- Authentication and authorization are mandatory for protected APIs.
- No decorative feature exists without an operational purpose.
