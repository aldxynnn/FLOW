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
let trackingMapHasFitted = false;
let trackingMapAutoFollow = true;
let mapLibrePromise = null;

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
  return `
    <div class="brand-mark" aria-hidden="true">
      <img
        src="assets/flow_logo.png"
        alt=""
        width="38"
        height="38"
        decoding="async"
      />
    </div>
  `;
}

function icon(name) {
  const paths = {
    overview: '<path d="M4 5.5A1.5 1.5 0 0 1 5.5 4h5A1.5 1.5 0 0 1 12 5.5v5A1.5 1.5 0 0 1 10.5 12h-5A1.5 1.5 0 0 1 4 10.5zM14 5.5A1.5 1.5 0 0 1 15.5 4h3A1.5 1.5 0 0 1 20 5.5v5A1.5 1.5 0 0 1 18.5 12h-3A1.5 1.5 0 0 1 14 10.5zM4 15.5A1.5 1.5 0 0 1 5.5 14h5a1.5 1.5 0 0 1 1.5 1.5v3A1.5 1.5 0 0 1 10.5 20h-5A1.5 1.5 0 0 1 4 18.5zM14 15.5a1.5 1.5 0 0 1 1.5-1.5h3a1.5 1.5 0 0 1 1.5 1.5v3a1.5 1.5 0 0 1-1.5 1.5h-3a1.5 1.5 0 0 1-1.5-1.5z"/>',
    trips: '<path d="M6.5 4.5h7A2.5 2.5 0 0 1 16 7v10.5A2.5 2.5 0 0 1 13.5 20h-7A2.5 2.5 0 0 1 4 17.5V7a2.5 2.5 0 0 1 2.5-2.5Zm0 3h7m-7 3.5h7m-7 3.5h4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>',
    users: '<path d="M15 20v-1.4a3.6 3.6 0 0 0-3.6-3.6H7.6A3.6 3.6 0 0 0 4 18.6V20m7-8a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Zm4.8 1a3.3 3.3 0 0 1 4.2 3.2V20" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>',
    profile: '<path d="M12 12.5a4.25 4.25 0 1 0 0-8.5 4.25 4.25 0 0 0 0 8.5Zm-7.5 7a7.5 7.5 0 0 1 15 0" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>',
    refresh: '<path d="M20 11a8 8 0 0 0-13.3-6L4.5 7.2M4.5 7.2V3.8m0 3.4h3.4M4 13a8 8 0 0 0 13.3 6l2.2-2.2m0 0v3.4m0-3.4h-3.4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>',
    logout: '<path d="M10 5H6.5A1.5 1.5 0 0 0 5 6.5v11A1.5 1.5 0 0 0 6.5 19H10m4-4 3-3-3-3m3 3H9" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>'
  };

  return `<svg viewBox="0 0 24 24" aria-hidden="true">${paths[name] || ''}</svg>`;
}

function renderLogin() {
  app.innerHTML = `
    <main class="auth">
      <div class="auth-shell">
        <section class="auth-intro">
          <div class="brand">
            ${logo()}
            <div>
              <b>FLOW</b>
              <span>Fleet & Operations</span>
            </div>
          </div>

          <div class="auth-copy">
            <span class="eyebrow">OPERATIONS WORKSPACE</span>
            <h1>Keep the day moving.</h1>
            <p>Plan trips, coordinate drivers, and monitor live execution from one focused workspace.</p>
          </div>

          <div class="auth-points">
            <div><span class="point">01</span><p><b>Plan clearly</b><span>Trips, vehicles, and assignments stay together.</span></p></div>
            <div><span class="point">02</span><p><b>Dispatch calmly</b><span>See active work without a wall of competing panels.</span></p></div>
            <div><span class="point">03</span><p><b>Track in real time</b><span>Live location and status stay close to the work.</span></p></div>
          </div>
        </section>

        <section class="auth-panel">
          <span class="auth-kicker">SECURE SIGN IN</span>
          <h2>Welcome back</h2>
          <p class="auth-subtitle">Use your FLOW account to access your operational workspace.</p>

          <form id="login">
            <label>
              Username
              <input name="username" autocomplete="username" required>
            </label>

            <label>
              Password
              <input name="password" type="password" autocomplete="current-password" required>
            </label>

            <button class="btn primary wide" type="submit">
              Sign in
            </button>
          </form>

          <p class="footnote">Access follows your assigned role and permissions.</p>
        </section>
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
            <span class="nav-icon">${icon('overview')}</span>
            <span>Overview</span>
          </button>

          <button class="nav" data-view="trips">
            <span class="nav-icon">${icon('trips')}</span>
            <span>Trips</span>
          </button>

          ${
            role === 'ADMIN'
              ? `
                <button class="nav" data-view="users">
                  <span class="nav-icon">${icon('users')}</span>
                  <span>People</span>
                </button>
              `
              : ''
          }

          <button class="nav" data-view="profile">
            <span class="nav-icon">${icon('profile')}</span>
            <span>Profile</span>
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
            <span>${icon('logout')}</span>
            <span>Sign out</span>
          </button>

        </div>

      </aside>

      <main class="content">

        <header class="top">

          <div>
            <span class="eyebrow">
              ${esc(role.replace('_', ' '))}
            </span>

            <h1 id="pageTitle">Overview</h1>
            <p id="pageSubtitle">Live operational activity and current fleet context.</p>
          </div>

          <button id="refresh" class="btn ghost">
            <span class="btn-icon">${icon('refresh')}</span>
            <span>Refresh</span>
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

  document.querySelectorAll('.nav').forEach(item => {
    item.classList.toggle('active', item.dataset.view === view);
  });

  pageTitle.textContent = title;

  const subtitles = {
    overview: 'Live operational activity and current fleet context.',
    trips: 'Plan, assign, and follow work from dispatch to completion.',
    users: 'Manage people, access, and account status.',
    profile: 'Account details and session information.'
  };

  const subtitleEl = document.getElementById('pageSubtitle');
  if (subtitleEl) subtitleEl.textContent = subtitles[view] || '';

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

  const active = cache.trips
    .filter(trip => trip.status === 'IN_PROGRESS')
    .sort((a, b) => {
      const timeA = new Date(
        a.lastLocationAt || a.startedAt || 0
      ).getTime();
      const timeB = new Date(
        b.lastLocationAt || b.startedAt || 0
      ).getTime();
      return timeB - timeA;
    });

  const assigned = cache.trips.filter(
    trip => trip.status === 'ASSIGNED'
  );

  const completed = cache.trips.filter(
    trip => trip.status === 'COMPLETED'
  );

  const cancelled = cache.trips.filter(
    trip => trip.status === 'CANCELLED'
  );

  const total = cache.trips.length;
  const completion = total
    ? Math.round((completed.length / total) * 100)
    : 0;

  const featuredTrip =
    active[0] ||
    assigned[0] ||
    cache.trips[0] ||
    null;

  const driverCopy = me.role === 'DRIVER';

  view.innerHTML = `
    <section class="ops-hero">
      <div class="ops-hero-copy">
        <div class="ops-eyebrow-row">
          <span class="eyebrow">
            ${driverCopy ? 'EXECUTION BOARD' : 'OPERATIONS BOARD'}
          </span>
          <span class="live-chip">
            <span class="live-dot"></span>
            ${active.length ? `${active.length} live` : 'Standby'}
          </span>
        </div>

        <h2>${
          driverCopy
            ? 'Keep your route in view.'
            : 'See the work. Keep it moving.'
        }</h2>

        <p>${
          driverCopy
            ? 'Assigned work, current trip status, and sync health without digging through screens.'
            : 'A focused command view for assignments, movement, and the next operational decision.'
        }</p>

        <div class="ops-route-summary">
          <div class="route-pin route-origin"><span></span></div>
          <div class="route-summary-copy">
            <span>${featuredTrip ? esc(featuredTrip.origin) : 'No active route'}</span>
            <b>${featuredTrip ? '→' : 'Waiting for dispatch'}</b>
            <span>${featuredTrip ? esc(featuredTrip.destination) : 'Create or assign a trip to begin'}</span>
          </div>
          <div class="route-pin route-destination"><span></span></div>
        </div>
      </div>

      <div class="ops-hero-visual" aria-hidden="true">
        <div class="route-stage">
          <div class="route-stage-label">CURRENT FLOW</div>
          <div class="route-line">
            <span class="route-node complete"></span>
            <span class="route-node active"></span>
            <span class="route-node idle"></span>
          </div>
          <div class="route-stage-meta">
            <span>Assigned</span>
            <strong>${active.length || assigned.length || 0}</strong>
            <span>Live</span>
          </div>
          <div class="route-stage-foot">
            <span>${featuredTrip ? esc(featuredTrip.code) : 'No trip selected'}</span>
            <span>${featuredTrip ? esc(featuredTrip.vehiclePlate || 'Vehicle pending') : '—'}</span>
          </div>
        </div>
      </div>
    </section>

    ${
      me.role !== 'DRIVER'
        ? `
          <section class="ops-metric-grid">
            <div class="ops-stat primary-stat">
              <span>Live now</span>
              <strong>${summary?.activeTrips ?? active.length}</strong>
              <small>${active.length ? 'Vehicles currently moving' : 'No vehicles moving'}</small>
            </div>
            <div class="ops-stat">
              <span>Assigned</span>
              <strong>${summary?.assignedTrips ?? assigned.length}</strong>
              <small>Waiting to depart</small>
            </div>
            <div class="ops-stat">
              <span>Completed</span>
              <strong>${summary?.completedTrips ?? completed.length}</strong>
              <small>${completion}% of visible trips</small>
            </div>
            <div class="ops-stat">
              <span>Fleet ready</span>
              <strong>${summary?.availableVehicles ?? 0}</strong>
              <small>${summary?.activeDrivers ?? 0} active drivers</small>
            </div>
          </section>
        `
        : `
          <section class="ops-driver-rail">
            <div class="ops-rail-step is-done">
              <span class="rail-index">01</span>
              <div><b>Assigned</b><small>${assigned.length} waiting</small></div>
            </div>
            <div class="ops-rail-line"></div>
            <div class="ops-rail-step ${active.length ? 'is-live' : ''}">
              <span class="rail-index">02</span>
              <div><b>On route</b><small>${active.length ? 'GPS active' : 'No live trip'}</small></div>
            </div>
            <div class="ops-rail-line"></div>
            <div class="ops-rail-step">
              <span class="rail-index">03</span>
              <div><b>Sync</b><small>Workspace connected</small></div>
            </div>
          </section>
        `
    }

    <section class="panel ops-panel">
      <div class="panel-head">
        <div>
          <span class="eyebrow">LIVE BOARD</span>
          <h3>${driverCopy ? 'Current work' : 'Active trips'}</h3>
          <p>${
            driverCopy
              ? 'Your assigned routes stay visible while you work.'
              : 'Every trip currently on the road is visible here, newest update first.'
          }</p>
        </div>

        <button class="btn ghost" onclick="showView('trips')">
          Open trip board
        </button>
      </div>

      <div class="ops-queue-head">
        <span>${active.length} live trips</span>
        <span>${completed.length} completed</span>
        <span>${cancelled.length} cancelled</span>
      </div>

      <div class="live-board-note">
        <span class="live-dot"></span>
        <span>All active trips are shown · newest GPS update first</span>
      </div>

      ${tripCards(active.length ? active : (driverCopy ? assigned : cache.trips.slice(0, 6)))}
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
  const canCreate = me.role === 'FLEET_MANAGER';
  const filter = window.flowTripFilter || 'ALL';
  const filtered = filter === 'ALL'
    ? cache.trips
    : cache.trips.filter(trip => trip.status === filter);

  const counts = {
    ALL: cache.trips.length,
    ASSIGNED: cache.trips.filter(trip => trip.status === 'ASSIGNED').length,
    IN_PROGRESS: cache.trips.filter(trip => trip.status === 'IN_PROGRESS').length,
    COMPLETED: cache.trips.filter(trip => trip.status === 'COMPLETED').length,
    CANCELLED: cache.trips.filter(trip => trip.status === 'CANCELLED').length
  };

  view.innerHTML = `
    <section class="panel trip-board">
      <div class="trip-board-top">
        <div>
          <span class="eyebrow">DISPATCH BOARD</span>
          <h3>Trips, without the clutter</h3>
          <p>Use status to narrow the board, then open a trip for live tracking or assignment.</p>
        </div>

        ${
          canCreate
            ? `
              <div class="actions">
                <button class="btn ghost" onclick="createVehicleForm()">Add vehicle</button>
                <button class="btn primary" onclick="createTripForm()">New trip</button>
              </div>
            `
            : ''
        }
      </div>

      <div class="trip-filterbar" role="tablist" aria-label="Trip status">
        ${[
          ['ALL', 'All'],
          ['ASSIGNED', 'Assigned'],
          ['IN_PROGRESS', 'In progress'],
          ['COMPLETED', 'Completed'],
          ['CANCELLED', 'Cancelled']
        ].map(([value, label]) => `
          <button
            class="trip-filter ${filter === value ? 'is-active' : ''}"
            role="tab"
            aria-selected="${filter === value}"
            onclick="setTripFilter('${value}')"
          >
            <span>${label}</span>
            <b>${counts[value]}</b>
          </button>
        `).join('')}
      </div>

      <div class="trip-board-note">
        <span class="live-dot"></span>
        Server state is the source of truth · ${filtered.length} ${filtered.length === 1 ? 'trip' : 'trips'} shown
      </div>

      ${tripCards(filtered)}
    </section>
  `;
}

function setTripFilter(filter) {
  window.flowTripFilter = filter;
  const view = document.getElementById('view');
  if (view) {
    trips(view);
  }
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
              OpenStreetMap · MapLibre
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

function sortTrackingPoints(points) {
  return (Array.isArray(points) ? points : [])
    .map(normalizeLocationPoint)
    .filter(Boolean)
    .sort((a, b) => {
      const timeA = a.recordedAt ? new Date(a.recordedAt).getTime() : 0;
      const timeB = b.recordedAt ? new Date(b.recordedAt).getTime() : 0;
      return timeA - timeB;
    });
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
    const live = cache.tripLocations[id] || [];
    const fallback =
      currentTrip?.latitude != null &&
      currentTrip?.longitude != null
        ? [{
            latitude: Number(currentTrip.latitude),
            longitude: Number(currentTrip.longitude),
            recordedAt:
              currentTrip.lastLocationAt ||
              new Date().toISOString()
          }]
        : [];

    cache.tripLocations[id] = sanitizeTrackingPoints(
      normalized.length || live.length || fallback.length
        ? [...normalized, ...live, ...fallback]
        : []
    );

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
   OPEN MAP RENDERER
   ========================================================= */

function loadMapLibre() {
  if (window.maplibregl) {
    return Promise.resolve(window.maplibregl);
  }

  if (mapLibrePromise) {
    return mapLibrePromise;
  }

  mapLibrePromise = import(
    'https://unpkg.com/maplibre-gl@6.11.2/dist/maplibre-gl.mjs'
  ).then(module => {
    window.maplibregl = module.default || module;
    return window.maplibregl;
  });

  return mapLibrePromise;
}

/* =========================================================
   MAP
   ========================================================= */

function destroyTrackingMap() {
  if (trackingMap) {
    trackingMap.remove();
  }

  trackingMap = null;
  trackingRoute = null;
  trackingMarker = null;
  trackingMapHasFitted = false;
  trackingMapAutoFollow = true;
  trackingMapTripId = null;
  trackingMapTrip = null;
}

function showMapError(message) {
  const mapError = document.getElementById('tripMapError');
  if (!mapError) return;

  mapError.hidden = false;
  mapError.innerHTML = `
    <strong>Map renderer unavailable</strong>
    <span>${esc(message)}</span>
  `;
}

function hideMapError() {
  const mapError = document.getElementById('tripMapError');
  if (!mapError) return;

  mapError.hidden = true;
  mapError.innerHTML = '';
}

function createTrackingVehicleMarker() {
  const element = document.createElement('div');
  element.className = 'flow-map-vehicle';
  return element;
}

function trackingGeoJson(points) {
  return {
    type: 'Feature',
    geometry: {
      type: 'LineString',
      coordinates: points.map(point => [
        point.longitude,
        point.latitude
      ])
    },
    properties: {}
  };
}

function fitTrackingMapToPoints(points) {
  if (!trackingMap || !points.length) return;

  const maps = window.maplibregl;
  if (!maps) return;

  if (points.length === 1) {
    trackingMap.jumpTo({
      center: [points[0].longitude, points[0].latitude],
      zoom: 14
    });
    trackingMapHasFitted = true;
    return;
  }

  const bounds = new maps.LngLatBounds();
  points.forEach(point => {
    bounds.extend([point.longitude, point.latitude]);
  });

  trackingMap.fitBounds(bounds, {
    padding: 48,
    maxZoom: 16,
    duration: 0
  });

  trackingMapHasFitted = true;
}

function createTrackingRecenterControl() {
  const control = {
    onAdd(map) {
      this.map = map;
      this.container = document.createElement('div');
      this.container.className = 'maplibregl-ctrl maplibregl-ctrl-group';
      this.button = document.createElement('button');
      this.button.type = 'button';
      this.button.className = 'flow-map-recenter';
      this.button.title = 'Center vehicle';
      this.button.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M12 2v4M12 18v4M2 12h4M18 12h4"></path></svg>';
      this.button.addEventListener('click', () => {
        trackingMapAutoFollow = true;
        updateRecenterButton();
        centerTrackingMapOnCurrent(true);
      });
      this.container.appendChild(this.button);
      return this.container;
    },
    onRemove() {
      this.container.remove();
      this.map = undefined;
    }
  };
  return control;
}

function updateRecenterButton() {
  const button = document.querySelector('.flow-map-recenter');
  if (button) button.classList.toggle('is-following', trackingMapAutoFollow);
}

function centerTrackingMapOnCurrent(animate) {
  if (!trackingMap || !trackingMapTripId) return;

  const points = sanitizeTrackingPoints(cache.tripLocations[trackingMapTripId] || []);
  const current = points[points.length - 1];
  if (!current) return;

  const center = [current.longitude, current.latitude];
  if (animate) {
    trackingMap.easeTo({
      center,
      zoom: Math.max(trackingMap.getZoom(), 14),
      duration: 500,
      essential: true
    });
  } else {
    trackingMap.jumpTo({
      center,
      zoom: Math.max(trackingMap.getZoom(), 14)
    });
  }
}

function renderMapLibreTrackingMap(pointsData) {
  const element = document.getElementById('tripMap');
  const maps = window.maplibregl;

  if (!element || !maps || trackingMapTripId == null) return;

  const ordered = sortTrackingPoints(pointsData);
  const current = ordered.length ? ordered[ordered.length - 1] : null;
  const first = ordered[0] || current;
  const center = current
    ? [current.longitude, current.latitude]
    : [106.8456, -6.2088];

  if (!trackingMap) {
    trackingMapAutoFollow = true;
    trackingMap = new maps.Map({
      container: element,
      style: 'https://vector.openstreetmap.org/styles/shortbread/graybeard.json',
      center,
      zoom: current ? 14 : 11,
      attributionControl: true,
      dragRotate: false,
      pitchWithRotate: false,
      maxPitch: 0
    });

    trackingMap.addControl(
      new maps.NavigationControl({ showCompass: false }),
      'bottom-right'
    );
    trackingMap.addControl(createTrackingRecenterControl(), 'bottom-right');
    trackingMap.on('dragstart', () => {
      trackingMapAutoFollow = false;
      updateRecenterButton();
    });

    trackingMap.once('load', () => {
      trackingMap.addSource('flow-route', {
        type: 'geojson',
        data: {
          type: 'FeatureCollection',
          features: []
        }
      });

      trackingMap.addLayer({
        id: 'flow-route-glow',
        type: 'line',
        source: 'flow-route',
        layout: { 'line-cap': 'round', 'line-join': 'round' },
        paint: { 'line-color': '#0B7A55', 'line-opacity': 0.14, 'line-width': 10 }
      });

      trackingMap.addLayer({
        id: 'flow-route',
        type: 'line',
        source: 'flow-route',
        layout: { 'line-cap': 'round', 'line-join': 'round' },
        paint: { 'line-color': '#0B7A55', 'line-opacity': 0.94, 'line-width': 4 }
      });

      updateRecenterButton();
      renderMapLibreTrackingMap(pointsData);
    });

    return;
  }

  const source = trackingMap.getSource('flow-route');
  if (source) {
    source.setData(
      ordered.length >= 2
        ? trackingGeoJson(ordered)
        : { type: 'FeatureCollection', features: [] }
    );
  }

  if (current) {
    const position = [current.longitude, current.latitude];

    if (!trackingMarker) {
      trackingMarker = new maps.Marker({
        element: createTrackingVehicleMarker(),
        anchor: 'center'
      }).setLngLat(position).addTo(trackingMap);
    } else {
      trackingMarker.setLngLat(position);
    }
  } else if (trackingMarker) {
    trackingMarker.remove();
    trackingMarker = null;
  }

  if (!trackingMapHasFitted) {
    fitTrackingMapToPoints(ordered);
    return;
  }

  if (current && trackingMapAutoFollow) {
    const view = trackingMap.getBounds();
    const currentPoint = new maps.LngLat(current.longitude, current.latitude);
    const outside = view ? !view.contains(currentPoint) : true;
    if (outside) {
      trackingMap.easeTo({
        center: currentPoint,
        zoom: Math.max(trackingMap.getZoom(), 14),
        duration: 500,
        essential: true
      });
    }
  }

  updateRecenterButton();
}

function updateTrackingMapData(locations) {
  const element = document.getElementById('tripMap');

  if (!element || trackingMapTripId == null) return;

  const fallbackPoint =
    trackingMapTrip?.latitude != null &&
    trackingMapTrip?.longitude != null
      ? [
          {
            latitude: Number(trackingMapTrip.latitude),
            longitude: Number(trackingMapTrip.longitude),
            recordedAt:
              trackingMapTrip.lastLocationAt ||
              new Date().toISOString()
          }
        ]
      : [];

  const pointsData = sanitizeTrackingPoints(
    Array.isArray(locations) && locations.length
      ? locations
      : fallbackPoint
  );

  const current = pointsData.length
    ? pointsData[pointsData.length - 1]
    : null;

  const empty = document.getElementById('tripMapEmpty');
  if (empty) {
    empty.hidden = pointsData.length > 0;

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

  const pointCount = document.getElementById('tripTrackingPointCount');
  if (pointCount) {
    pointCount.textContent =
      `${pointsData.length} ${pointsData.length === 1 ? 'point' : 'points'}`;
  }

  const position = document.getElementById('tripTrackingPosition');
  if (position) {
    position.textContent = current
      ? `${current.latitude.toFixed(5)}, ${current.longitude.toFixed(5)}`
      : 'Waiting';
  }

  const lastUpdate = document.getElementById('tripTrackingLastUpdate');
  if (lastUpdate) {
    lastUpdate.textContent = current?.recordedAt
      ? new Date(current.recordedAt).toLocaleString()
      : '—';
  }

  if (!pointsData.length) return;

  if (!window.maplibregl) {
    loadMapLibre()
      .then(() => {
        if (trackingMapTripId === trackingMapTrip?.id) {
          renderMapLibreTrackingMap(pointsData);
        }
      })
      .catch(error => {
        console.error('MapLibre load error:', error);
        showMapError(
          error.message || 'MapLibre could not be loaded.'
        );
      });

    return;
  }

  hideMapError();
  renderMapLibreTrackingMap(pointsData);
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