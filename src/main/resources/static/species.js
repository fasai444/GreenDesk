const API_SPECIES = '/api/species';
const API_GREENHOUSE_OVERVIEW = '/api/greenhouse/overview';

let speciesCache = [];

document.addEventListener('DOMContentLoaded', async () => {
    if (!await AUTH.requireAuth()) return;
    loadSpeciesTable();
});

function showSpeciesFeedback(message, variant = 'info') {
    const feedback = document.getElementById('speciesFeedback');
    if (!feedback) return;
    feedback.className = `alert alert-${variant} app-feedback mb-3`;
    feedback.textContent = message;
    feedback.classList.remove('d-none');
}

function clearSpeciesFeedback() {
    const feedback = document.getElementById('speciesFeedback');
    if (!feedback) return;
    feedback.classList.add('d-none');
    feedback.textContent = '';
}

async function getJson(url, options = {}) {
    const response = await fetch(url, {
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options
    });

    const contentType = response.headers.get('content-type') || '';
    const payload = contentType.includes('application/json') ? await response.json() : await response.text();

    if (!response.ok) {
        const msg = typeof payload === 'string' ? payload : payload?.error || payload?.message || `HTTP ${response.status}`;
        throw new Error(msg);
    }

    return payload;
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function normalize(value) {
    return String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
}

function filteredSpecies() {
    const query = normalize(document.getElementById('speciesSearch')?.value || '');
    const list = [...speciesCache].sort((left, right) => String(left.name || '').localeCompare(String(right.name || '')));
    if (!query) return list;
    return list.filter(species => normalize(species.name).includes(query));
}

function renderSpeciesRows(list) {
    const tbody = document.getElementById('speciesTableBody');
    if (!tbody) return;

    if (!list.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">Aucune espèce trouvée.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(s => `
        <tr>
            <td class="fw-bold">${escapeHtml(s.name)}</td>
            <td><span class="badge bg-info text-dark">${s.optimalWaterNeeds} ml</span></td>
            <td>${s.optimalLuxNeeds}</td>
            <td>${s.optimalTemperature}°C</td>
            <td>x${s.baseGrowthRate}</td>
            <td class="text-end">
                <button class="btn btn-sm btn-outline-danger" onclick="deleteSpecies('${s.id}')">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function updateSpeciesCounters(list) {
    const total = document.getElementById('speciesCount');
    if (total) total.textContent = String(list.length);
}

async function loadSpeciesTable() {
    clearSpeciesFeedback();

    const refreshBtn = document.getElementById('speciesRefreshBtn');
    if (refreshBtn) {
        refreshBtn.disabled = true;
        refreshBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Chargement...';
    }

    try {
        speciesCache = await getJson(API_SPECIES);
        const visible = filteredSpecies();
        renderSpeciesRows(visible);
        updateSpeciesCounters(visible);
        await loadSpeciesOpsSnapshot();
    } catch (error) {
        showSpeciesFeedback(`Erreur chargement espèces: ${error.message}`, 'danger');
    } finally {
        if (refreshBtn) {
            refreshBtn.disabled = false;
            refreshBtn.innerHTML = '<i class="fas fa-rotate"></i> Rafraîchir';
        }
    }
}

async function loadSpeciesOpsSnapshot() {
    const alerts = document.getElementById('speciesOpsAlerts');
    const effects = document.getElementById('speciesOpsEffects');
    if (!alerts || !effects) return;

    try {
        const overview = await getJson(API_GREENHOUSE_OVERVIEW);
        alerts.textContent = String(overview?.activeAlerts ?? 0);
        effects.textContent = String(overview?.activeEffects ?? 0);
    } catch (_) {
        alerts.textContent = 'N/A';
        effects.textContent = 'N/A';
    }
}

async function createSpecies() {
    clearSpeciesFeedback();

    const rawName = document.getElementById('spName').value.trim();
    const data = {
        name: rawName,
        optimalWaterNeeds: parseFloat(document.getElementById('spWater').value),
        optimalTemperature: parseFloat(document.getElementById('spTemp').value),
        optimalLuxNeeds: parseFloat(document.getElementById('spLux').value),
        optimalHumidity: parseFloat(document.getElementById('spHum').value),
        baseGrowthRate: 1.0,
        seedProductionRate: 0.5
    };

    if (!data.name) {
        showSpeciesFeedback('Le nom est obligatoire.', 'warning');
        return;
    }

    if ([data.optimalWaterNeeds, data.optimalTemperature, data.optimalLuxNeeds, data.optimalHumidity].some(value => Number.isNaN(value))) {
        showSpeciesFeedback('Tous les paramètres numériques doivent être valides.', 'warning');
        return;
    }

    if (data.optimalWaterNeeds <= 0 || data.optimalLuxNeeds <= 0 || data.optimalHumidity < 0) {
        showSpeciesFeedback('Valeurs incohérentes: eau/lumière > 0, humidité >= 0.', 'warning');
        return;
    }

    const submitBtn = document.getElementById('speciesCreateBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Enregistrement...';
    }

    try {
        await getJson(API_SPECIES, {
            method: 'POST',
            body: JSON.stringify(data)
        });

        const modal = bootstrap.Modal.getInstance(document.getElementById('createSpeciesModal'));
        modal?.hide();
        document.getElementById('spName').value = '';
        showSpeciesFeedback('Espèce créée avec succès.', 'success');
        await loadSpeciesTable();
    } catch (error) {
        showSpeciesFeedback(`Erreur création: ${error.message}`, 'danger');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Enregistrer';
        }
    }
}

async function deleteSpecies(id) {
    if (!confirm("Supprimer cette espèce ?")) return;

    clearSpeciesFeedback();

    try {
        await getJson(`${API_SPECIES}/${id}`, { method: 'DELETE' });
        showSpeciesFeedback('Espèce supprimée.', 'success');
        await loadSpeciesTable();
    } catch (error) {
        showSpeciesFeedback(`Suppression impossible: ${error.message}`, 'danger');
    }
}

function onSpeciesSearchInput() {
    const visible = filteredSpecies();
    renderSpeciesRows(visible);
    updateSpeciesCounters(visible);
}

document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
        const search = document.getElementById('speciesSearch');
        if (!search || !search.value) return;
        search.value = '';
        onSpeciesSearchInput();
    }
});