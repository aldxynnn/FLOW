# FLOW maps

FLOW uses MapLibre GL JS with OpenStreetMap Shortbread vector tiles.

- No Google Maps API key.
- No Google Cloud billing account.
- The web and Android tracking views use the same subdued vector-map direction.
- Visible OpenStreetMap attribution is required and is kept in the map controls.

The current OSM vector tile endpoint is:
`https://vector.openstreetmap.org/shortbread_v1/{z}/{x}/{y}.mvt`

The map style is based on OpenStreetMap's published Shortbread `graybeard` style.
See OpenStreetMap's current vector-tile usage policy before publishing at scale.
