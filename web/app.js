const API = 'http://localhost:8080/api';

let token = localStorage.getItem('flow_token');
let me = null;
let socket = null;
let socketRetryTimer = null;

let trackingMap = null;
let trackingMapTripId = null;
let trackingMapTrip = null;
let trackingRoute = null;
let trackingMarker = null;
let trackingTileLayer = null;
let trackingTileFallbackLayer = null;
let trackingMapHasFitted = false;
let leafletPromise = null;

const app = document.getElementById('app');

const esc = value =>
  String(value ?? '').replace(
    /[&<>'"]/g,
    char => ({
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      "'": '&#39;',
      '"': '&quot;'
    }[char])
  );

function toast(message) {
  const el = document.createElement('div');
  el.className = 'toast';
  el.textContent = message;
  document.body.appendChild(el);

  setTimeout(() => {
    el.remove();
  }, 2800);
}

/* =========================================================
   API
   ========================================================= */

async function api(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API}${path}`, {
    ...options,
    headers
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(
      data.message ||
      data.error ||
      `Request failed (${response.status})`
    );
  }

  return data;
}

/* =========================================================
   AUTH
   ========================================================= */

function logo() {
  return `<div class="brand-mark">F</div>`;
}

function renderLogin() {
  app.innerHTML = `
    <main class="auth">
      <div class="auth-panel">

        <div class="brand">
          ${logo()}
          <div>
            <b>FLOW</b>
            <span>Fleet & Operations</span>
          </div>
        </div>

        <div class="auth-copy">
          <span class="eyebrow">OPERATIONS PLATFORM</span>
          <h1>Move every trip with clarity.</h1>
          <p>
            One workspace for planning, dispatch, execution and fleet visibility.
          </p>
        </div>

        <form id="login">
          <label>
            Username
            <input
              name="username"
              autocomplete="username"
              required
            >
          </label>

          <label>
            Password
            <input
              name="password"
              type="password"
              autocomplete="current-password"
              required
            >
          </label>

          <button class="btn primary wide" type="submit">
            Sign in
          </button>
        </form>

        <p class="footnote">
          Access is determined by your assigned role.
        </p>

      </div>
    </main>
  `;

  const form = document.getElementById('login');

  form.onsubmit = async event => {
    event.preventDefault();

    const button = form.querySelector('button');
    const formData = new FormData(form);

    button.disabled = true;
    button.textContent = 'Signing in...';

    try {
      const data = await api('/auth/login', {
        method: 'POST',
        body: JSON.stringify({
          username: formData.get('username'),
          password: formData.get('password')
        })
      });

      if (!data.accessToken) {
        throw new Error('Login berhasil tetapi access token tidak ditemukan.');
      }

      token = data.accessToken;
      localStorage.setItem('flow_token', token);

      await boot();

    } catch (error) {
      console.error('FLOW login error:', error);

      token = null;
      localStorage.removeItem('flow_token');

      toast(error.message || 'Login failed');

      button.disabled = false;
      button.textContent = 'Sign in';
    }
  };
}

async function boot() {
  try {
    me = await api('/auth/me');

    if (!me || !me.username || !me.role) {
      throw new Error('Invalid account response.');
    }

    renderShell();

    await load();

    connect();

  } catch (error) {
    console.error('FLOW boot error:', error);

    if (
      error.message?.includes('401') ||
      error.message?.includes('403') ||
      error.message?.toLowerCase().includes('unauthorized') ||
      error.message?.toLowerCase().includes('forbidden')
    ) {
      token = null;
      localStorage.removeItem('flow_token');
      renderLogin();
      toast('Session expired. Please sign in again.');
      return;
    }

    if (me) {
      if (!document.querySelector('.app-shell')) {
        renderShell();
      }

      toast(
        error.message ||
        'Some operational data could not be loaded.'
      );

      connect();
      return;
    }

    token = null;
    localStorage.removeItem('flow_token');
    renderLogin();
  }
}

/* =========================================================
   SHELL
   ========================================================= */

function renderShell() {
  const role = me.role;

  app.innerHTML = `
    <div class="app-shell">

      <aside>

        <div class="brand side">
          ${logo()}
          <div>
            <b>FLOW</b>
            <span>Fleet & Operations</span>
          </div>
        </div>

        <nav>
          <button class="nav active" data-view="overview">
            Overview
          </button>

          <button class="nav" data-view="trips">
            Trips
          </button>

          ${
            role === 'ADMIN'
              ? `
                <button class="nav" data-view="users">
                  People
                </button>
              `
              : ''
          }

          <button class="nav" data-view="profile">
            Profile
          </button>
        </nav>

        <div class="side-footer">

          <div class="user-mini">
            <span class="avatar">
              ${esc(me.username?.[0]?.toUpperCase())}
            </span>

            <div>
              <b>${esc(me.username)}</b>
              <small>${esc(role.replace('_', ' '))}</small>
            </div>
          </div>

          <button id="logout" class="logout">
            Sign out
          </button>

        </div>

      </aside>

      <main class="content">

        <header class="top">

          <div>
            <span class="eyebrow">
              ${esc(role.replace('_', ' '))}
            </span>

            <h1 id="pageTitle">
              Overview
            </h1>
          </div>

          <button id="refresh" class="btn ghost">
            Refresh
          </button>

        </header>

        <div id="view"></div>

      </main>

    </div>
  `;

  document.querySelectorAll('.nav').forEach(button => {
    button.onclick = () => {
      document
        .querySelectorAll('.nav')
        .forEach(item => item.classList.remove('active'));

      button.classList.add('active');

      showView(button.dataset.view);
    };
  });

  document.getElementById('refresh').onclick = load;

  document.getElementById('logout').onclick = () => {
    disconnectSocket();

    token = null;
    me = null;

    localStorage.removeItem('flow_token');

    renderLogin();
  };

  showView('overview');
}

/* =========================================================
   DATA
   ========================================================= */

let cache = {
  summary: null,
  trips: [],
  drivers: [],
  vehicles: [],
  users: [],
  tripLocations: {}
};

async function load() {
  if (!me) {
    return;
  }

  try {
    const common = [
      api('/trips')
    ];

    if (me.role !== 'DRIVER') {
      common.push(
        api('/dashboard/summary'),
        api('/drivers'),
        api('/vehicles')
      );
    }

    if (me.role === 'ADMIN') {
      common.push(
        api('/users')
      );
    }

    const results = await Promise.all(common);

    cache.trips = results[0] || [];

    if (me.role !== 'DRIVER') {
      cache.summary = results[1] || null;
      cache.drivers = results[2] || [];
      cache.vehicles = results[3] || [];
    }

    if (me.role === 'ADMIN') {
      cache.users = results[4] || [];
    }

    const activeView =
      document.querySelector('.nav.active')?.dataset.view ||
      'overview';

    showView(activeView);

  } catch (error) {
    console.error('FLOW data loading error:', error);

    toast(
      error.message ||
      'Failed to load operational data.'
    );
  }
}

/* =========================================================
   VIEW ROUTING
   ========================================================= */

function showView(view) {
  destroyTrackingMap();

  const title =
    view === 'users'
      ? 'People & access'
      : view.charAt(0).toUpperCase() + view.slice(1);

  const pageTitle = document.getElementById('pageTitle');
  const viewContainer = document.getElementById('view');

  if (!pageTitle || !viewContainer) {
    return;
  }

  pageTitle.textContent = title;

  if (view === 'profile') {
    return profile(viewContainer);
  }

  if (view === 'users') {
    return users(viewContainer);
  }

  if (view === 'trips') {
    return trips(viewContainer);
  }

  overview(viewContainer);
}

/* =========================================================
   OVERVIEW
   ========================================================= */

function overview(view) {
  const summary = cache.summary;

  const active = cache.trips.filter(
    trip => trip.status === 'IN_PROGRESS'
  );

  const assigned = cache.trips.filter(
    trip => trip.status === 'ASSIGNED'
  );

  view.innerHTML = `
    <section class="hero">

      <div>
        <span class="eyebrow">
          ${me.role === 'DRIVER' ? 'EXECUTION' : 'LIVE OPERATIONS'}
        </span>

        <h2>
          ${
            me.role === 'DRIVER'
              ? 'Your route board'
              : 'The fleet at a glance'
          }
        </h2>

        <p>
          ${
            me.role === 'DRIVER'
              ? 'Assigned work, active route and sync status in one place.'
              : 'Plan, dispatch and monitor from the same operational source of truth.'
          }
        </p>
      </div>

      <div class="hero-stat">
        <strong>
          ${
            me.role === 'DRIVER'
              ? active.length
              : (summary?.activeTrips ?? 0)
          }
        </strong>

        <span>active trips</span>
      </div>

    </section>

    ${
      me.role !== 'DRIVER'
        ? `
          <div class="metrics">

            <div class="metric">
              <span>Assigned</span>
              <b>${summary?.assignedTrips ?? 0}</b>
            </div>

            <div class="metric">
              <span>Completed</span>
              <b>${summary?.completedTrips ?? 0}</b>
            </div>

            <div class="metric">
              <span>Drivers</span>
              <b>${summary?.activeDrivers ?? 0}</b>
            </div>

            <div class="metric">
              <span>Vehicles ready</span>
              <b>${summary?.availableVehicles ?? 0}</b>
            </div>

          </div>
        `
        : ''
    }

    <section class="panel">

      <div class="panel-head">

        <div>
          <h3>
            ${
              me.role === 'DRIVER'
                ? 'Assigned work'
                : 'Recent trips'
            }
          </h3>

          <p>
            Live data from the operational database.
          </p>
        </div>

        <button
          class="btn ghost"
          onclick="showView('trips')"
        >
          Open trips
        </button>

      </div>

      ${tripCards(
        me.role === 'DRIVER'
          ? assigned
          : cache.trips.slice(0, 5)
      )}

    </section>
  `;
}

function tripCards(items) {
  if (!items.length) {
    return `
      <div class="empty">
        <b>No operational records yet.</b>
        <span>
          Create a trip or wait for dispatch to assign one.
        </span>
      </div>
    `;
  }

  return `
    <div class="trip-list">

      ${items.map(trip => `
        <button
          class="trip-card"
          onclick="showTrip(${trip.id})"
        >

          <div>

            <span class="code">
              ${esc(trip.code)}
            </span>

            <h4>
              ${esc(trip.origin)}
              <i>→</i>
              ${esc(trip.destination)}
            </h4>

            <p>
              ${esc(trip.driverUsername)}
              ·
              ${esc(trip.vehiclePlate)}
            </p>

          </div>

          <span class="badge ${esc(trip.status)}">
            ${esc(trip.status.replace('_', ' '))}
          </span>

        </button>
      `).join('')}

    </div>
  `;
}

/* =========================================================
   TRIPS
   ========================================================= */

function trips(view) {
  const canCreate =
    me.role === 'FLEET_MANAGER';

  view.innerHTML = `
    <section class="panel">

      <div class="panel-head">

        <div>
          <h3>Trip board</h3>

          <p>
            Assignments and status are always read from the server.
          </p>
        </div>

        ${
          canCreate
            ? `
              <div class="actions">

                <button
                  class="btn ghost"
                  onclick="createVehicleForm()"
                >
                  Add vehicle
                </button>

                <button
                  class="btn primary"
                  onclick="createTripForm()"
                >
                  New trip
                </button>

              </div>
            `
            : ''
        }

      </div>

      ${tripCards(cache.trips)}

    </section>
  `;
}

/* =========================================================
   TRIP DETAIL
   ========================================================= */

function showTrip(id) {
  const trip = cache.trips.find(item => item.id === id);

  if (!trip) {
    return;
  }

  destroyTrackingMap();

  trackingMapTripId = id;
  trackingMapTrip = trip;

  const dispatch = me.role === 'DISPATCHER';
  const view = document.getElementById('view');

  if (!view) {
    return;
  }

  const locations = sanitizeTrackingPoints(
    cache.tripLocations[id] || []
  );

  if (locations.length) {
    cache.tripLocations[id] = locations;
  }

  const latest =
    locations.length
      ? locations[locations.length - 1]
      : null;

  view.innerHTML = `
    <section class="panel detail">

      <button
        class="back"
        onclick="showView('trips')"
      >
        ← Back to trips
      </button>

      <div class="detail-top">

        <div>
          <span class="code">${esc(trip.code)}</span>

          <h2>
            ${esc(trip.origin)}
            →
            ${esc(trip.destination)}
          </h2>

          <p>
            ${esc(trip.driverUsername)}
            ·
            ${esc(trip.vehiclePlate)}
          </p>
        </div>

        <span class="badge ${esc(trip.status)}">
          ${esc(trip.status.replace('_', ' '))}
        </span>

      </div>

      <div class="tracking-card">

        <div class="tracking-head">

          <div>
            <span class="tracking-kicker">
              LIVE TRACKING
            </span>

            <h3>
              Vehicle location
            </h3>

            <p>
              ${
                latest
                  ? 'Realtime GPS trail and current position'
                  : 'Waiting for GPS signal from the driver'
              }
            </p>
          </div>

          <span
            id="tripTrackingPointCount"
            class="tracking-count"
          >
            ${locations.length} ${locations.length === 1 ? 'point' : 'points'}
          </span>

        </div>

        <div class="tracking-map-wrap">

          <div
            id="tripMap"
            class="tracking-map"
          >
            <div
              id="tripMapError"
              class="tracking-map-error"
              hidden
            ></div>
          </div>

          <div
            id="tripMapEmpty"
            class="tracking-map-empty"
            ${latest ? 'hidden' : ''}
          >
            <strong>
              ${
                trip.status === 'IN_PROGRESS'
                  ? 'Waiting for GPS'
                  : 'GPS starts with the trip'
              }
            </strong>

            <span>
              ${
                trip.status === 'IN_PROGRESS'
                  ? 'The next location update will appear here automatically.'
                  : 'Start the trip on Android to begin live tracking.'
              }
            </span>
          </div>

        </div>

        <div class="tracking-meta">

          <div>
            <span>Current position</span>

            <b id="tripTrackingPosition">
              ${
                latest
                  ? `${Number(latest.latitude).toFixed(5)}, ${Number(latest.longitude).toFixed(5)}`
                  : (
                      trip.latitude == null
                        ? 'Waiting'
                        : `${Number(trip.latitude).toFixed(5)}, ${Number(trip.longitude).toFixed(5)}`
                    )
              }
            </b>
          </div>

          <div>
            <span>Last update</span>

            <b id="tripTrackingLastUpdate">
              ${
                latest?.recordedAt
                  ? new Date(latest.recordedAt).toLocaleString()
                  : (
                      trip.lastLocationAt
                        ? new Date(trip.lastLocationAt).toLocaleString()
                        : '—'
                    )
              }
            </b>
          </div>

          <div>
            <span>Map</span>

            <b>
              OpenStreetMap · © OpenStreetMap contributors
            </b>
          </div>

        </div>

      </div>

      <div class="detail-grid">

        <div>
          <span>ETA</span>
          <b>${trip.etaMinutes} min</b>
        </div>

        <div>
          <span>Status</span>
          <b>
            ${esc(trip.status.replace('_', ' '))}
          </b>
        </div>

        <div>
          <span>Last location</span>
          <b>
            ${
              trip.lastLocationAt
                ? new Date(trip.lastLocationAt).toLocaleString()
                : '—'
            }
          </b>
        </div>

      </div>

      ${
        dispatch && trip.status !== 'COMPLETED'
          ? `
            <div class="assign">

              <h3>
                Reassign driver
              </h3>

              <select id="assignDriver">
                ${cache.drivers
                  .filter(driver => driver.enabled)
                  .map(driver => `
                    <option value="${esc(driver.username)}">
                      ${esc(driver.username)}
                    </option>
                  `)
                  .join('')}
              </select>

              <button
                class="btn primary"
                onclick="assignTrip(${trip.id})"
              >
                Save assignment
              </button>

            </div>
          `
          : ''
      }

    </section>
  `;

  updateTrackingMapData(locations);
  loadTripLocations(id);
}

/* =========================================================
   GPS DATA NORMALIZATION
   ========================================================= */

function normalizeLocationPoint(point) {
  if (!point) {
    return null;
  }

  const latitude = Number(point.latitude);
  const longitude = Number(point.longitude);

  if (
    !Number.isFinite(latitude) ||
    !Number.isFinite(longitude) ||
    latitude < -90 ||
    latitude > 90 ||
    longitude < -180 ||
    longitude > 180
  ) {
    return null;
  }

  return {
    latitude,
    longitude,
    recordedAt: point.recordedAt || null
  };
}

function haversineKm(a, b) {
  const earthRadiusKm = 6371;

  const lat1 = a.latitude * Math.PI / 180;
  const lat2 = b.latitude * Math.PI / 180;

  const deltaLat =
    (b.latitude - a.latitude) * Math.PI / 180;

  const deltaLon =
    (b.longitude - a.longitude) * Math.PI / 180;

  const sinLat =
    Math.sin(deltaLat / 2);

  const sinLon =
    Math.sin(deltaLon / 2);

  const value =
    sinLat * sinLat +
    Math.cos(lat1) *
    Math.cos(lat2) *
    sinLon *
    sinLon;

  return 2 *
    earthRadiusKm *
    Math.atan2(
      Math.sqrt(value),
      Math.sqrt(Math.max(0, 1 - value))
    );
}

/*
 * Map-only cleanup.
 *
 * It does NOT modify backend data.
 *
 * If the historical GPS list contains an old coordinate that
 * suddenly jumps thousands of kilometres to the current trip,
 * the latest continuous segment is used for visualization.
 *
 * This specifically prevents old emulator locations such as
 * Mountain View from being connected to the current Jakarta/BSD
 * route.
 */
function sanitizeTrackingPoints(points) {
  const normalized = Array.isArray(points)
    ? points
        .map(normalizeLocationPoint)
        .filter(Boolean)
    : [];

  if (!normalized.length) {
    return [];
  }

  normalized.sort((a, b) => {
    const timeA = a.recordedAt
      ? new Date(a.recordedAt).getTime()
      : 0;

    const timeB = b.recordedAt
      ? new Date(b.recordedAt).getTime()
      : 0;

    return timeA - timeB;
  });

  const deduplicated = [];

  for (const point of normalized) {
    const previous =
      deduplicated[deduplicated.length - 1];

    if (
      previous &&
      Math.abs(previous.latitude - point.latitude) < 0.000001 &&
      Math.abs(previous.longitude - point.longitude) < 0.000001 &&
      previous.recordedAt === point.recordedAt
    ) {
      continue;
    }

    deduplicated.push(point);
  }

  if (deduplicated.length <= 1) {
    return deduplicated;
  }

  /*
   * Split whenever two consecutive points are more than
   * 1,000 km apart.
   *
   * A normal fleet GPS trail should not jump from Indonesia
   * to California between adjacent recorded points.
   */
  const MAX_CONTIGUOUS_JUMP_KM = 1000;

  const segments = [];
  let currentSegment = [deduplicated[0]];

  for (let index = 1; index < deduplicated.length; index += 1) {
    const previous =
      deduplicated[index - 1];

    const current =
      deduplicated[index];

    const distance =
      haversineKm(previous, current);

    if (distance > MAX_CONTIGUOUS_JUMP_KM) {
      if (currentSegment.length) {
        segments.push(currentSegment);
      }

      currentSegment = [current];
    } else {
      currentSegment.push(current);
    }
  }

  if (currentSegment.length) {
    segments.push(currentSegment);
  }

  /*
   * Use the newest segment, which represents the current
   * operational tracking session.
   */
  return segments.length
    ? segments[segments.length - 1]
    : deduplicated;
}

function appendTrackingPoint(existing, point) {
  const current =
    Array.isArray(existing)
      ? existing
      : [];

  const normalized =
    normalizeLocationPoint(point);

  if (!normalized) {
    return sanitizeTrackingPoints(current);
  }

  const combined = [
    ...current,
    normalized
  ].slice(-500);

  return sanitizeTrackingPoints(combined);
}

/* =========================================================
   LOAD GPS HISTORY
   ========================================================= */

async function loadTripLocations(id) {
  try {
    const locations = await api(
      `/trips/${id}/locations`
    );

    const normalized =
      sanitizeTrackingPoints(
        Array.isArray(locations)
          ? locations
          : []
      );

    const currentTrip =
      cache.trips.find(item => item.id === id);

    /*
     * If history has not been persisted yet but the current
     * trip already has a location, use that as fallback.
     */
    if (
      !normalized.length &&
      currentTrip?.latitude != null &&
      currentTrip?.longitude != null
    ) {
      cache.tripLocations[id] = [
        {
          latitude: Number(currentTrip.latitude),
          longitude: Number(currentTrip.longitude),
          recordedAt:
            currentTrip.lastLocationAt ||
            new Date().toISOString()
        }
      ];
    } else {
      cache.tripLocations[id] = normalized;
    }

    if (
      trackingMapTripId === id &&
      document.getElementById('tripMap')
    ) {
      updateTrackingMapData(
        cache.tripLocations[id]
      );
    }

  } catch (error) {
    console.warn(
      'Trip location history error:',
      error
    );

    const currentTrip =
      cache.trips.find(item => item.id === id);

    const fallback =
      currentTrip?.latitude != null &&
      currentTrip?.longitude != null
        ? [
            {
              latitude:
                Number(currentTrip.latitude),
              longitude:
                Number(currentTrip.longitude),
              recordedAt:
                currentTrip.lastLocationAt ||
                new Date().toISOString()
            }
          ]
        : [];

    cache.tripLocations[id] =
      sanitizeTrackingPoints(fallback);

    if (
      trackingMapTripId === id &&
      document.getElementById('tripMap')
    ) {
      updateTrackingMapData(
        cache.tripLocations[id]
      );
    }
  }
}

/* =========================================================
   LEAFLET LOADER
   ========================================================= */

function loadLeaflet() {
  if (window.L) {
    return Promise.resolve(window.L);
  }

  if (leafletPromise) {
    return leafletPromise;
  }

  leafletPromise = new Promise((resolve, reject) => {
    const existingScript =
      document.querySelector(
        'script[data-flow-leaflet="true"]'
      );

    const existingStylesheet =
      document.querySelector(
        'link[data-flow-leaflet="true"]'
      );

    const ensureCss = () => {
      if (existingStylesheet) {
        return;
      }

      const link =
        document.createElement('link');

      link.rel = 'stylesheet';
      link.href =
        'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      link.dataset.flowLeaflet = 'true';

      document.head.appendChild(link);
    };

    ensureCss();

    if (existingScript) {
      existingScript.addEventListener(
        'load',
        () => {
          if (window.L) {
            resolve(window.L);
          } else {
            reject(
              new Error(
                'Leaflet loaded but global L is unavailable.'
              )
            );
          }
        },
        { once: true }
      );

      existingScript.addEventListener(
        'error',
        () => {
          reject(
            new Error(
              'Leaflet script failed to load.'
            )
          );
        },
        { once: true }
      );

      return;
    }

    const script =
      document.createElement('script');

    script.src =
      'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';

    script.async = true;
    script.dataset.flowLeaflet = 'true';

    script.onload = () => {
      if (window.L) {
        resolve(window.L);
      } else {
        reject(
          new Error(
            'Leaflet loaded but global L is unavailable.'
          )
        );
      }
    };

    script.onerror = () => {
      /*
       * CDN fallback.
       */
      const fallback =
        document.createElement('script');

      fallback.src =
        'https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js';

      fallback.async = true;
      fallback.dataset.flowLeaflet = 'true';

      fallback.onload = () => {
        if (window.L) {
          resolve(window.L);
        } else {
          reject(
            new Error(
              'Leaflet fallback loaded but global L is unavailable.'
            )
          );
        }
      };

      fallback.onerror = () => {
        reject(
          new Error(
            'Unable to load Leaflet.'
          )
        );
      };

      document.head.appendChild(fallback);
    };

    document.head.appendChild(script);
  });

  return leafletPromise;
}

/* =========================================================
   MAP
   ========================================================= */

function destroyTrackingMap() {
  if (trackingMap) {
    trackingMap.remove();
    trackingMap = null;
  }

  trackingRoute = null;
  trackingMarker = null;
  trackingTileLayer = null;
  trackingTileFallbackLayer = null;
  trackingMapHasFitted = false;

  trackingMapTripId = null;
  trackingMapTrip = null;
}

function showMapError(message) {
  const mapError =
    document.getElementById('tripMapError');

  if (!mapError) {
    return;
  }

  mapError.hidden = false;

  mapError.innerHTML = `
    <strong>Map unavailable</strong>
    <span>${esc(message)}</span>
  `;
}

function hideMapError() {
  const mapError =
    document.getElementById('tripMapError');

  if (!mapError) {
    return;
  }

  mapError.hidden = true;
  mapError.innerHTML = '';
}

function createTileLayers(map) {
  /*
   * Primary: OpenStreetMap.
   */
  const osm =
    L.tileLayer(
      'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        maxZoom: 19,
        minZoom: 2,
        tileSize: 256,
        keepBuffer: 3,
        updateWhenIdle: true,
        updateWhenZooming: false,
        attribution:
          '&copy; OpenStreetMap contributors'
      }
    );

  /*
   * Fallback: Esri World Street Map.
   *
   * No CARTO is used anymore, avoiding:
   * "API 1 KEY REQUIRED"
   */
  const esri =
    L.tileLayer(
      'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}',
      {
        maxZoom: 19,
        minZoom: 2,
        tileSize: 256,
        keepBuffer: 3,
        updateWhenIdle: true,
        updateWhenZooming: false,
        attribution:
          'Tiles &copy; Esri'
      }
    );

  let osmErrorCount = 0;
  let switchedToFallback = false;

  osm.on('tileerror', () => {
    osmErrorCount += 1;

    if (
      osmErrorCount >= 3 &&
      !switchedToFallback
    ) {
      switchedToFallback = true;

      if (map.hasLayer(osm)) {
        map.removeLayer(osm);
      }

      trackingTileLayer = esri;
      trackingTileFallbackLayer = esri;

      esri.addTo(map);
      hideMapError();
      map.invalidateSize(true);
    }
  });

  esri.on('tileload', () => {
    hideMapError();
  });

  esri.on('tileerror', () => {
    if (switchedToFallback) {
      showMapError(
        'Map tiles could not be reached. GPS tracking data is still available.'
      );
    }
  });

  trackingTileLayer = osm;
  trackingTileFallbackLayer = esri;

  osm.addTo(map);
}

function fitTrackingMapToPoints(points, animate = false) {
  if (
    !trackingMap ||
    !points.length
  ) {
    return;
  }

  if (points.length === 1) {
    const point =
      points[0];

    trackingMap.setView(
      [
        point.latitude,
        point.longitude
      ],
      14,
      {
        animate
      }
    );

    trackingMapHasFitted = true;
    return;
  }

  const bounds =
    L.latLngBounds(points);

  trackingMap.fitBounds(
    bounds,
    {
      padding: [48, 48],
      maxZoom: 16,
      animate
    }
  );

  trackingMapHasFitted = true;
}

function updateTrackingMapData(locations) {
  const element =
    document.getElementById('tripMap');

  if (
    !element ||
    trackingMapTripId == null
  ) {
    return;
  }

  const fallbackPoint =
    trackingMapTrip?.latitude != null &&
    trackingMapTrip?.longitude != null
      ? [
          {
            latitude:
              Number(trackingMapTrip.latitude),
            longitude:
              Number(trackingMapTrip.longitude),
            recordedAt:
              trackingMapTrip.lastLocationAt ||
              new Date().toISOString()
          }
        ]
      : [];

  const pointsData =
    sanitizeTrackingPoints(
      Array.isArray(locations) &&
      locations.length
        ? locations
        : fallbackPoint
    );

  const current =
    pointsData.length
      ? pointsData[pointsData.length - 1]
      : null;

  const empty =
    document.getElementById(
      'tripMapEmpty'
    );

  if (empty) {
    empty.hidden =
      pointsData.length > 0;

    if (!pointsData.length) {
      empty.innerHTML = `
        <strong>
          ${
            trackingMapTrip?.status === 'IN_PROGRESS'
              ? 'Waiting for GPS'
              : 'GPS starts with the trip'
          }
        </strong>

        <span>
          ${
            trackingMapTrip?.status === 'IN_PROGRESS'
              ? 'The next location update will appear here automatically.'
              : 'Start the trip on Android to begin live tracking.'
          }
        </span>
      `;
    } else {
      empty.innerHTML = '';
    }
  }

  const pointCount =
    document.getElementById(
      'tripTrackingPointCount'
    );

  if (pointCount) {
    pointCount.textContent =
      `${pointsData.length} ${pointsData.length === 1 ? 'point' : 'points'}`;
  }

  const position =
    document.getElementById(
      'tripTrackingPosition'
    );

  if (position) {
    position.textContent =
      current
        ? `${current.latitude.toFixed(5)}, ${current.longitude.toFixed(5)}`
        : 'Waiting';
  }

  const lastUpdate =
    document.getElementById(
      'tripTrackingLastUpdate'
    );

  if (lastUpdate) {
    lastUpdate.textContent =
      current?.recordedAt
        ? new Date(
            current.recordedAt
          ).toLocaleString()
        : '—';
  }

  if (!pointsData.length) {
    return;
  }

  /*
   * Leaflet may not yet be loaded.
   */
  if (!window.L) {
    loadLeaflet()
      .then(() => {
        if (
          trackingMapTripId === trackingMapTrip?.id ||
          document.getElementById('tripMap')
        ) {
          updateTrackingMapData(pointsData);
        }
      })
      .catch(error => {
        console.error(
          'Leaflet load error:',
          error
        );

        if (empty) {
          empty.hidden = false;
          empty.innerHTML = `
            <strong>Map renderer unavailable</strong>
            <span>
              GPS data is available, but the map renderer could not be loaded.
            </span>
          `;
        }

        showMapError(
          'Leaflet could not be loaded.'
        );
      });

    return;
  }

  hideMapError();

  /*
   * First map creation.
   */
  if (!trackingMap) {
    const first =
      pointsData[0];

    const center =
      first
        ? [
            first.latitude,
            first.longitude
          ]
        : [
            -6.2088,
            106.8456
          ];

    trackingMap =
      L.map(
        element,
        {
          zoomControl: true,
          attributionControl: true,
          preferCanvas: true
        }
      ).setView(
        center,
        first ? 14 : 11
      );

    trackingMap.attributionControl.setPrefix('');

    createTileLayers(
      trackingMap
    );

    trackingMap.whenReady(() => {
      trackingMap.invalidateSize(true);

      setTimeout(() => {
        trackingMap?.invalidateSize(true);
      }, 100);
    });
  }

  /*
   * Render route.
   */
  const points =
    pointsData.map(point => [
      point.latitude,
      point.longitude
    ]);

  if (trackingRoute) {
    trackingRoute.setLatLngs(points);
  } else {
    trackingRoute =
      L.polyline(
        points,
        {
          color: '#00B14F',
          weight: 5,
          opacity: 0.9,
          lineCap: 'round',
          lineJoin: 'round'
        }
      ).addTo(
        trackingMap
      );
  }

  /*
   * Render current marker.
   */
  if (trackingMarker) {
    if (current) {
      trackingMarker.setLatLng([
        current.latitude,
        current.longitude
      ]);
    }
  } else if (current) {
    trackingMarker =
      L.circleMarker(
        [
          current.latitude,
          current.longitude
        ],
        {
          radius: 8,
          color: '#FFFFFF',
          weight: 3,
          fillColor: '#00B14F',
          fillOpacity: 1
        }
      ).addTo(
        trackingMap
      );
  }

  trackingMap.invalidateSize(true);

  /*
   * Initial fit only.
   *
   * We intentionally don't call fitBounds on every
   * realtime point, otherwise the map jumps/zooms
   * constantly while the vehicle moves.
   */
  if (!trackingMapHasFitted) {
    fitTrackingMapToPoints(
      points,
      false
    );
    return;
  }

  /*
   * After initial fit, gently keep the current marker
   * visible without resetting the user's zoom.
   */
  if (current) {
    const currentLatLng =
      L.latLng(
        current.latitude,
        current.longitude
      );

    if (
      !trackingMap.getBounds().pad(-0.18).contains(
        currentLatLng
      )
    ) {
      trackingMap.panTo(
        currentLatLng,
        {
          animate: true,
          duration: 0.4
        }
      );
    }
  }
}

/* =========================================================
   ASSIGNMENT
   ========================================================= */

async function assignTrip(id) {
  try {
    const select =
      document.getElementById(
        'assignDriver'
      );

    if (!select) {
      return;
    }

    await api(
      `/trips/${id}/assign`,
      {
        method: 'POST',
        body: JSON.stringify({
          driverUsername:
            select.value
        })
      }
    );

    toast(
      'Assignment updated'
    );

    await load();

    showTrip(id);

  } catch (error) {
    console.error(
      'Assignment error:',
      error
    );

    toast(
      error.message
    );
  }
}

/* =========================================================
   VEHICLE
   ========================================================= */

function createVehicleForm() {
  const view =
    document.getElementById(
      'view'
    );

  view.innerHTML = `
    <section class="panel form-panel">

      <button
        class="back"
        onclick="showView('trips')"
      >
        ← Back
      </button>

      <h2>Add vehicle</h2>

      <p>
        Add a real vehicle to the available fleet.
      </p>

      <form
        id="vehicleForm"
        class="form-grid"
      >

        <label>
          Plate number
          <input
            name="plate"
            required
          >
        </label>

        <label>
          Model
          <input
            name="model"
            required
          >
        </label>

        <label>
          Type
          <input
            name="type"
            required
          >
        </label>

        <div>
          <button
            class="btn primary"
            type="submit"
          >
            Add vehicle
          </button>
        </div>

      </form>

    </section>
  `;

  document.getElementById(
    'vehicleForm'
  ).onsubmit =
    async event => {
      event.preventDefault();

      const formData =
        new FormData(
          event.target
        );

      try {
        await api(
          '/vehicles',
          {
            method: 'POST',
            body: JSON.stringify({
              plateNumber:
                formData.get('plate'),
              model:
                formData.get('model'),
              type:
                formData.get('type'),
              status:
                'AVAILABLE'
            })
          }
        );

        toast(
          'Vehicle added'
        );

        await load();

        showView(
          'trips'
        );

      } catch (error) {
        console.error(
          'Vehicle creation error:',
          error
        );

        toast(
          error.message
        );
      }
    };
}

/* =========================================================
   CREATE TRIP
   ========================================================= */

function createTripForm() {
  const drivers =
    cache.drivers.filter(
      driver =>
        driver.enabled
    );

  const vehicles =
    cache.vehicles.filter(
      vehicle =>
        vehicle.status ===
        'AVAILABLE'
    );

  const view =
    document.getElementById(
      'view'
    );

  view.innerHTML = `
    <section class="panel form-panel">

      <button
        class="back"
        onclick="showView('trips')"
      >
        ← Back
      </button>

      <h2>New trip</h2>

      <p>
        Choose from currently available drivers and vehicles.
        Nothing is pre-filled.
      </p>

      <form
        id="tripForm"
        class="form-grid"
      >

        <label>
          Origin
          <input
            name="origin"
            required
          >
        </label>

        <label>
          Destination
          <input
            name="destination"
            required
          >
        </label>

        <label>
          ETA minutes
          <input
            name="eta"
            type="number"
            min="0"
            required
          >
        </label>

        <label>
          Driver

          <select
            name="driver"
            required
          >
            ${drivers
              .map(driver => `
                <option
                  value="${esc(driver.username)}"
                >
                  ${esc(driver.username)}
                </option>
              `)
              .join('')}
          </select>

        </label>

        <label>
          Vehicle

          <select
            name="vehicle"
            required
          >
            ${vehicles
              .map(vehicle => `
                <option
                  value="${esc(vehicle.plateNumber)}"
                >
                  ${esc(vehicle.plateNumber)}
                  ·
                  ${esc(vehicle.model)}
                </option>
              `)
              .join('')}
          </select>

        </label>

        <div>
          <button
            class="btn primary"
            type="submit"
          >
            Create trip
          </button>
        </div>

      </form>

    </section>
  `;

  document.getElementById(
    'tripForm'
  ).onsubmit =
    async event => {
      event.preventDefault();

      const formData =
        new FormData(
          event.target
        );

      try {
        await api(
          '/trips',
          {
            method: 'POST',
            body: JSON.stringify({
              origin:
                formData.get('origin'),
              destination:
                formData.get('destination'),
              etaMinutes:
                Number(
                  formData.get('eta')
                ),
              driverUsername:
                formData.get('driver'),
              vehiclePlate:
                formData.get('vehicle')
            })
          }
        );

        toast(
          'Trip created'
        );

        await load();

        showView(
          'trips'
        );

      } catch (error) {
        console.error(
          'Trip creation error:',
          error
        );

        toast(
          error.message
        );
      }
    };
}

/* =========================================================
   USERS
   ========================================================= */

function users(view) {
  view.innerHTML = `
    <section class="panel">

      <div class="panel-head">

        <div>
          <h3>
            People & access
          </h3>

          <p>
            Admin-controlled accounts.
            No seeded demo records are required.
          </p>
        </div>

        <button
          class="btn primary"
          onclick="createUserForm()"
        >
          New user
        </button>

      </div>

      <div class="people">

        ${
          cache.users.length
            ? cache.users
                .map(user => `
                  <div class="person">

                    <div>
                      <b>
                        ${esc(user.username)}
                      </b>

                      <span>
                        ${esc(
                          user.role.replace(
                            '_',
                            ' '
                          )
                        )}
                      </span>
                    </div>

                    <button
                      class="btn ghost"
                      onclick="toggleUser(
                        ${user.id},
                        ${!user.enabled}
                      )"
                    >
                      ${
                        user.enabled
                          ? 'Disable'
                          : 'Enable'
                      }
                    </button>

                  </div>
                `)
                .join('')
            : `
              <div class="empty">
                <b>
                  No users found.
                </b>

                <span>
                  Create the first operational account.
                </span>
              </div>
            `
        }

      </div>

    </section>
  `;
}

function createUserForm() {
  const view =
    document.getElementById(
      'view'
    );

  view.innerHTML = `
    <section class="panel form-panel">

      <button
        class="back"
        onclick="showView('users')"
      >
        ← Back
      </button>

      <h2>
        Create user
      </h2>

      <form
        id="userForm"
        class="form-grid"
      >

        <label>
          Username
          <input
            name="username"
            required
          >
        </label>

        <label>
          Temporary password
          <input
            name="password"
            type="password"
            minlength="8"
            required
          >
        </label>

        <label>
          Role

          <select name="role">
            <option>
              DRIVER
            </option>

            <option>
              DISPATCHER
            </option>

            <option>
              FLEET_MANAGER
            </option>
          </select>

        </label>

        <div>
          <button
            class="btn primary"
            type="submit"
          >
            Create user
          </button>
        </div>

      </form>

    </section>
  `;

  document.getElementById(
    'userForm'
  ).onsubmit =
    async event => {
      event.preventDefault();

      const formData =
        new FormData(
          event.target
        );

      try {
        await api(
          '/users',
          {
            method: 'POST',
            body: JSON.stringify({
              username:
                formData.get(
                  'username'
                ),
              password:
                formData.get(
                  'password'
                ),
              role:
                formData.get(
                  'role'
                ),
              enabled: true
            })
          }
        );

        toast(
          'User created'
        );

        await load();

        showView(
          'users'
        );

      } catch (error) {
        console.error(
          'User creation error:',
          error
        );

        toast(
          error.message
        );
      }
    };
}

async function toggleUser(
  id,
  enabled
) {
  try {
    await api(
      `/users/${id}/enabled`,
      {
        method: 'PATCH',
        body: JSON.stringify({
          enabled
        })
      }
    );

    await load();

  } catch (error) {
    console.error(
      'User status error:',
      error
    );

    toast(
      error.message
    );
  }
}

/* =========================================================
   PROFILE
   ========================================================= */

function profile(view) {
  view.innerHTML = `
    <section class="panel profile">

      <span class="eyebrow">
        ACCOUNT
      </span>

      <h2>
        ${esc(me.username)}
      </h2>

      <p>
        ${esc(
          me.role.replace(
            '_',
            ' '
          )
        )}
      </p>

      <div class="detail-grid">

        <div>
          <span>
            Access model
          </span>

          <b>
            Role-based
          </b>
        </div>

        <div>
          <span>
            Session
          </span>

          <b>
            JWT
          </b>
        </div>

        <div>
          <span>
            Data
          </span>

          <b>
            Operational database
          </b>
        </div>

      </div>

    </section>
  `;
}

/* =========================================================
   WEBSOCKET
   ========================================================= */

function disconnectSocket() {
  if (socketRetryTimer) {
    clearTimeout(
      socketRetryTimer
    );

    socketRetryTimer = null;
  }

  if (socket) {
    socket.onclose = null;
    socket.close();
    socket = null;
  }
}

function connect() {
  if (!token) {
    return;
  }

  if (socketRetryTimer) {
    clearTimeout(
      socketRetryTimer
    );

    socketRetryTimer = null;
  }

  try {
    if (socket) {
      socket.onclose = null;
      socket.close();
    }

    const websocketProtocol =
      API.startsWith('https://')
        ? 'wss'
        : 'ws';

    const websocketHost =
      new URL(API).host;

    socket =
      new WebSocket(
        `${websocketProtocol}://${websocketHost}/ws/ops?access_token=${encodeURIComponent(token)}`
      );

    socket.onopen = () => {
      console.info(
        'FLOW realtime connected'
      );
    };

    socket.onmessage = async event => {
      try {
        const message =
          JSON.parse(
            event.data
          );

        if (
          message?.type ===
            'trip.location' &&
          message?.data?.id
        ) {
          const trip =
            message.data;

          const id =
            Number(
              trip.id
            );

          cache.trips =
            cache.trips.map(
              item =>
                item.id === id
                  ? {
                      ...item,
                      ...trip
                    }
                  : item
            );

          if (
            trip.latitude != null &&
            trip.longitude != null
          ) {
            const point = {
              latitude:
                trip.latitude,
              longitude:
                trip.longitude,
              recordedAt:
                trip.lastLocationAt ||
                new Date().toISOString()
            };

            const current =
              cache.tripLocations[id] ||
              [];

            cache.tripLocations[id] =
              appendTrackingPoint(
                current,
                point
              );

            if (
              trackingMapTripId === id &&
              document.getElementById(
                'tripMap'
              )
            ) {
              updateTrackingMapData(
                cache.tripLocations[id]
              );

              const pointCount =
                document.getElementById(
                  'tripTrackingPointCount'
                );

              const position =
                document.getElementById(
                  'tripTrackingPosition'
                );

              const lastUpdate =
                document.getElementById(
                  'tripTrackingLastUpdate'
                );

              if (pointCount) {
                pointCount.textContent =
                  `${cache.tripLocations[id].length} ${cache.tripLocations[id].length === 1 ? 'point' : 'points'}`;
              }

              if (position) {
                position.textContent =
                  `${Number(point.latitude).toFixed(5)}, ${Number(point.longitude).toFixed(5)}`;
              }

              if (lastUpdate) {
                lastUpdate.textContent =
                  new Date(
                    point.recordedAt
                  ).toLocaleString();
              }
            }
          }

          return;
        }

        await load();

      } catch (error) {
        console.error(
          'FLOW realtime refresh error:',
          error
        );
      }
    };

    socket.onerror =
      error => {
        console.warn(
          'FLOW realtime connection error',
          error
        );
      };

    socket.onclose =
      () => {
        socket = null;

        if (!token) {
          return;
        }

        socketRetryTimer =
          setTimeout(
            connect,
            4000
          );
      };

  } catch (error) {
    console.warn(
      'FLOW realtime setup error:',
      error
    );

    socketRetryTimer =
      setTimeout(
        connect,
        4000
      );
  }
}

/* =========================================================
   START
   ========================================================= */

if (token) {
  boot();
} else {
  renderLogin();
}