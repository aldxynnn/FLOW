# FLOW maps — portfolio/no-billing setup

This project intentionally does not use Google Maps Platform.

Web and Android use MapLibre-based rendering with OpenStreetMap map data/style resources. There is no Google Maps API key, Google Cloud billing account, or Google Maps SDK dependency required for map rendering.

For portfolio/demo traffic, keep usage light, retain OpenStreetMap attribution, and follow the current tile/style usage policies of the upstream providers.

## GPS behavior

The vehicle marker uses the newest valid GPS point by `recordedAt`. Live points are merged with server history instead of letting an older history response overwrite an already-received live point.

The map initially focuses on the active vehicle. While auto-follow is enabled, a new point outside the current viewport recenters the map. Dragging disables auto-follow until the center control is pressed.
