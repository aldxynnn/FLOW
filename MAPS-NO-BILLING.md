# MAPS — portfolio setup

This portfolio build intentionally does **not** use Google Maps Platform.

Map rendering is handled by MapLibre GL JS / MapLibre Native-style web rendering, while the basemap comes from OpenStreetMap's published Shortbread vector tiles and style resources.

There is no Google API key and no Google Cloud billing configuration in this build.

For a portfolio/demo, keep map usage light and respect OpenStreetMap's current vector tile usage policy and attribution requirements.
