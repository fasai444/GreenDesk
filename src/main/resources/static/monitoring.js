// monitoring.js (L4-A) — improved UI with plant dropdown
const API_BASE = ""; // keep "" when served by Spring Boot at localhost:8080

function qs(id) { return document.getElementById(id); }

function showError(msg) {
  const el = qs("monitoringError");
  if (el) el.textContent = msg || "";
}

async function apiFetch(path, options = {}) {
  const res = await fetch(API_BASE + path, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }

  const contentType = res.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) return null;
  return res.json();
}

/**
 * Try multiple endpoints because projects differ:
 * - /plants
 * - /api/plants
 */
async function loadPlantsList() {
  const candidates = ["/plants", "/api/plants"];
  let plants = null;
  let lastErr = null;

  for (const url of candidates) {
    try {
      plants = await apiFetch(url);
      if (Array.isArray(plants)) return plants;
    } catch (e) {
      lastErr = e;
    }
  }
  throw lastErr || new Error("Could not load plants list (no endpoint found).");
}

function plantLabel(p) {
  // Try best label based on your model fields
  const name = p.name || p.plantName || p.label || "Plant";
  const species = (p.species && (p.species.name || p.speciesName)) || p.speciesName || "";
  const forest = p.forestId ? ` • forest:${p.forestId}` : "";
  return species ? `${name} (${species})${forest}` : `${name}${forest}`;
}

function setPlantSelectOptions(plants) {
  const sel = qs("plantSelect");
  if (!sel) return;

  sel.innerHTML = `<option value="">-- Choose a plant --</option>`;

  for (const p of plants) {
    const id = p.id || p._id; // Spring may return id or _id
    if (!id) continue;

    const opt = document.createElement("option");
    opt.value = id;
    opt.textContent = plantLabel(p);
    sel.appendChild(opt);
  }
}

function renderAlerts(alerts) {
  const ul = qs("alertsList");
  if (!ul) return;
  ul.innerHTML = "";

  if (!alerts || alerts.length === 0) {
    ul.innerHTML = "<li>No active alerts ✅</li>";
    return;
  }

  for (const a of alerts) {
    const li = document.createElement("li");

    li.innerHTML = `
      <div style="display:flex;justify-content:space-between;gap:10px;align-items:flex-start;">
        <div>
          <div style="font-weight:700;">${a.severity} • <code>${a.type}</code></div>
          <div style="margin-top:6px;color:#374151;">${a.message}</div>
          <div style="margin-top:6px;color:#6b7280;font-size:12px;">${a.createdAt || ""}</div>
        </div>
        <button data-id="${a.id}" class="l4-btn l4-btn-outline">Ack</button>
      </div>
    `;

    li.querySelector("button").addEventListener("click", async (e) => {
      try {
        showError("");
        const id = e.target.getAttribute("data-id");
        await apiFetch(`/alerts/${id}/ack`, { method: "POST" });
        await loadMonitoring();
      } catch (err) {
        showError(err.message);
      }
    });

    ul.appendChild(li);
  }
}

function renderReadings(readings) {
  const ul = qs("readingsList");
  if (!ul) return;
  ul.innerHTML = "";

  if (!readings || readings.length === 0) {
    ul.innerHTML = "<li>No readings yet</li>";
    return;
  }

  for (const r of readings.slice(0, 10)) {
    const li = document.createElement("li");
    li.innerHTML = `
      <div style="font-weight:600;">${r.timestamp || ""}</div>
      <div style="margin-top:6px;color:#374151;">
        T=${r.temperature}°C • H=${r.humidity}% • Lux=${r.lux} • Rain=${r.rainfall}
      </div>
    `;
    ul.appendChild(li);
  }
}

function getSelectedPlantId() {
  // Preferred: dropdown
  const sel = qs("plantSelect");
  if (sel && sel.value) return sel.value.trim();

  // fallback: hidden input (or manual if you ever add it back)
  return (qs("plantIdInput")?.value || "").trim();
}

async function loadMonitoring() {
  const plantId = getSelectedPlantId();
  if (!plantId) {
    showError("Please choose a plant.");
    return;
  }

  // keep hidden input synced (compat)
  const hidden = qs("plantIdInput");
  if (hidden) hidden.value = plantId;

  try {
    showError("");

    const [alerts, readings] = await Promise.all([
      apiFetch(`/plants/${plantId}/alerts?active=true`),
      apiFetch(`/plants/${plantId}/sensor-readings`)
    ]);

    renderAlerts(alerts || []);
    renderReadings(readings || []);
  } catch (err) {
    showError(err.message);
  }
}

async function sendReading() {
  const plantId = getSelectedPlantId();
  if (!plantId) {
    showError("Please choose a plant.");
    return;
  }

  const body = {
    temperature: Number(qs("tempInput")?.value),
    humidity: Number(qs("humInput")?.value),
    lux: Number(qs("luxInput")?.value),
    rainfall: Number(qs("rainInput")?.value),
  };

  if ([body.temperature, body.humidity, body.lux, body.rainfall].some(Number.isNaN)) {
    showError("Please fill all sensor fields with numbers.");
    return;
  }

  try {
    showError("");
    await apiFetch(`/plants/${plantId}/sensor-readings`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    await loadMonitoring();
  } catch (err) {
    showError(err.message);
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  try {
    showError("");
    const plants = await loadPlantsList();
    setPlantSelectOptions(plants);

    qs("plantSelect")?.addEventListener("change", () => {
      showError("");
      // auto-load when selecting plant
      loadMonitoring();
    });

    qs("sendReadingBtn")?.addEventListener("click", sendReading);
    qs("l4RefreshBtn")?.addEventListener("click", loadMonitoring);
  } catch (err) {
    showError(
      "Could not load plants list. You may need a GET /plants endpoint. Error: " + err.message
    );
    const sel = qs("plantSelect");
    if (sel) sel.innerHTML = `<option value="">(Could not load plants)</option>`;
  }
});