const API = 'http://localhost:8080/api';

let token = localStorage.getItem('flow_token');
let me = null;
let socket = null;
let socketRetryTimer = null;

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

    /*
     * Jangan langsung hapus token untuk error data/dashboard.
     * Token hanya dihapus kalau authentication memang gagal.
     */
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

    /*
     * Jika auth/me berhasil tetapi data dashboard gagal,
     * shell tetap dipertahankan.
     */
    if (me) {
      if (!document.querySelector('.app-shell')) {
        renderShell();
      }

      toast(error.message || 'Some operational data could not be loaded.');
      connect();
      return;
    }

    token = null;
    localStorage.removeItem('flow_token');
    renderLogin();

  }
}

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
          <button
            class="nav active"
            data-view="overview"
          >
            Overview
          </button>

          <button
            class="nav"
            data-view="trips"
          >
            Trips
          </button>

          ${
            role === 'ADMIN'
              ? `
                <button
                  class="nav"
                  data-view="users"
                >
                  People
                </button>
              `
              : ''
          }

          <button
            class="nav"
            data-view="profile"
          >
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

          <button
            id="logout"
            class="logout"
          >
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

          <button
            id="refresh"
            class="btn ghost"
          >
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

let cache = {
  summary: null,
  trips: [],
  drivers: [],
  vehicles: [],
  users: []
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

function showView(view) {
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

function showTrip(id) {
  const trip = cache.trips.find(
    item => item.id === id
  );

  if (!trip) {
    return;
  }

  const dispatch =
    me.role === 'DISPATCHER';

  const view =
    document.getElementById('view');

  if (!view) {
    return;
  }

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

          <span class="code">
            ${esc(trip.code)}
          </span>

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

      <div class="detail-grid">

        <div>
          <span>ETA</span>
          <b>${trip.etaMinutes} min</b>
        </div>

        <div>
          <span>GPS</span>

          <b>
            ${
              trip.latitude == null
                ? 'Waiting'
                : `${Number(trip.latitude).toFixed(5)}, ${Number(trip.longitude).toFixed(5)}`
            }
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

              <h3>Reassign driver</h3>

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
}

async function assignTrip(id) {
  try {
    const select =
      document.getElementById('assignDriver');

    if (!select) {
      return;
    }

    await api(`/trips/${id}/assign`, {
      method: 'POST',
      body: JSON.stringify({
        driverUsername: select.value
      })
    });

    toast('Assignment updated');

    await load();

    showTrip(id);

  } catch (error) {
    console.error('Assignment error:', error);
    toast(error.message);
  }
}

function createVehicleForm() {
  const view =
    document.getElementById('view');

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
          <input name="plate" required>
        </label>

        <label>
          Model
          <input name="model" required>
        </label>

        <label>
          Type
          <input name="type" required>
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

  document.getElementById('vehicleForm').onsubmit =
    async event => {
      event.preventDefault();

      const formData =
        new FormData(event.target);

      try {
        await api('/vehicles', {
          method: 'POST',
          body: JSON.stringify({
            plateNumber: formData.get('plate'),
            model: formData.get('model'),
            type: formData.get('type'),
            status: 'AVAILABLE'
          })
        });

        toast('Vehicle added');

        await load();

        showView('trips');

      } catch (error) {
        console.error('Vehicle creation error:', error);
        toast(error.message);
      }
    };
}

function createTripForm() {
  const drivers =
    cache.drivers.filter(driver => driver.enabled);

  const vehicles =
    cache.vehicles.filter(
      vehicle => vehicle.status === 'AVAILABLE'
    );

  const view =
    document.getElementById('view');

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
          <input name="origin" required>
        </label>

        <label>
          Destination
          <input name="destination" required>
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

          <select name="driver" required>
            ${drivers.map(driver => `
              <option value="${esc(driver.username)}">
                ${esc(driver.username)}
              </option>
            `).join('')}
          </select>

        </label>

        <label>
          Vehicle

          <select name="vehicle" required>
            ${vehicles.map(vehicle => `
              <option value="${esc(vehicle.plateNumber)}">
                ${esc(vehicle.plateNumber)}
                ·
                ${esc(vehicle.model)}
              </option>
            `).join('')}
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

  document.getElementById('tripForm').onsubmit =
    async event => {
      event.preventDefault();

      const formData =
        new FormData(event.target);

      try {
        await api('/trips', {
          method: 'POST',
          body: JSON.stringify({
            origin: formData.get('origin'),
            destination: formData.get('destination'),
            etaMinutes: Number(formData.get('eta')),
            driverUsername: formData.get('driver'),
            vehiclePlate: formData.get('vehicle')
          })
        });

        toast('Trip created');

        await load();

        showView('trips');

      } catch (error) {
        console.error('Trip creation error:', error);
        toast(error.message);
      }
    };
}

function users(view) {
  view.innerHTML = `
    <section class="panel">

      <div class="panel-head">

        <div>
          <h3>People & access</h3>

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
            ? cache.users.map(user => `
                <div class="person">

                  <div>
                    <b>
                      ${esc(user.username)}
                    </b>

                    <span>
                      ${esc(user.role.replace('_', ' '))}
                    </span>
                  </div>

                  <button
                    class="btn ghost"
                    onclick="toggleUser(
                      ${user.id},
                      ${!user.enabled}
                    )"
                  >
                    ${user.enabled ? 'Disable' : 'Enable'}
                  </button>

                </div>
              `).join('')
            : `
              <div class="empty">
                <b>No users found.</b>
                <span>Create the first operational account.</span>
              </div>
            `
        }

      </div>

    </section>
  `;
}

function createUserForm() {
  const view =
    document.getElementById('view');

  view.innerHTML = `
    <section class="panel form-panel">

      <button
        class="back"
        onclick="showView('users')"
      >
        ← Back
      </button>

      <h2>Create user</h2>

      <form
        id="userForm"
        class="form-grid"
      >

        <label>
          Username
          <input name="username" required>
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

            <option>DRIVER</option>
            <option>DISPATCHER</option>
            <option>FLEET_MANAGER</option>

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

  document.getElementById('userForm').onsubmit =
    async event => {
      event.preventDefault();

      const formData =
        new FormData(event.target);

      try {
        await api('/users', {
          method: 'POST',
          body: JSON.stringify({
            username: formData.get('username'),
            password: formData.get('password'),
            role: formData.get('role'),
            enabled: true
          })
        });

        toast('User created');

        await load();

        showView('users');

      } catch (error) {
        console.error('User creation error:', error);
        toast(error.message);
      }
    };
}

async function toggleUser(id, enabled) {
  try {
    await api(`/users/${id}/enabled`, {
      method: 'PATCH',
      body: JSON.stringify({
        enabled
      })
    });

    await load();

  } catch (error) {
    console.error('User status error:', error);
    toast(error.message);
  }
}

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
        ${esc(me.role.replace('_', ' '))}
      </p>

      <div class="detail-grid">

        <div>
          <span>Access model</span>
          <b>Role-based</b>
        </div>

        <div>
          <span>Session</span>
          <b>JWT</b>
        </div>

        <div>
          <span>Data</span>
          <b>Operational database</b>
        </div>

      </div>

    </section>
  `;
}

function disconnectSocket() {
  if (socketRetryTimer) {
    clearTimeout(socketRetryTimer);
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
    clearTimeout(socketRetryTimer);
    socketRetryTimer = null;
  }

  try {
    if (socket) {
      socket.onclose = null;
      socket.close();
    }

    /*
     * Frontend berjalan di :5500,
     * tetapi WebSocket berada di Spring Boot :8080.
     */
    const websocketProtocol =
      API.startsWith('https://')
        ? 'wss'
        : 'ws';

    const websocketHost =
      new URL(API).host;

    socket = new WebSocket(
      `${websocketProtocol}://${websocketHost}/ws/ops?access_token=${encodeURIComponent(token)}`
    );

    socket.onopen = () => {
      console.info('FLOW realtime connected');
    };

    socket.onmessage = async () => {
      try {
        await load();
      } catch (error) {
        console.error(
          'FLOW realtime refresh error:',
          error
        );
      }
    };

    socket.onerror = error => {
      console.warn(
        'FLOW realtime connection error',
        error
      );
    };

    socket.onclose = () => {
      socket = null;

      if (!token) {
        return;
      }

      socketRetryTimer = setTimeout(
        connect,
        4000
      );
    };

  } catch (error) {
    console.warn(
      'FLOW realtime setup error:',
      error
    );

    socketRetryTimer = setTimeout(
      connect,
      4000
    );
  }
}

if (token) {
  boot();
} else {
  renderLogin();
}