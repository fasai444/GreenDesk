const API_EFFECTS = '/api/effects';
const API_GREENHOUSE_OVERVIEW = '/api/greenhouse/overview';

let effectsCache = [];

document.addEventListener('DOMContentLoaded', async () => {
    if (!await AUTH.requireAuth()) return;
    loadEffects();
});

function showEffectsFeedback(message, variant = 'info') {
    const feedback = document.getElementById('effectsFeedback');
    if (!feedback) return;
    feedback.className = `alert alert-${variant} app-feedback mb-3`;
    feedback.textContent = message;
    feedback.classList.remove('d-none');
}

function clearEffectsFeedback() {
    const feedback = document.getElementById('effectsFeedback');
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

function renderModifierRow(label, value) {
    if (!value) return '';
    return `<small class="text-muted d-block">${label}: ${value}</small>`;
}

function normalize(value) {
    return String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
}

function filteredEffects() {
    const query = normalize(document.getElementById('effectsSearch')?.value || '');
    const mode = document.getElementById('effectsScope')?.value || 'ALL';

    let list = [...effectsCache].sort((left, right) => String(left.name || '').localeCompare(String(right.name || '')));
    if (mode === 'CUSTOM') list = list.filter(e => e.isCustom);
    if (mode === 'SYSTEM') list = list.filter(e => !e.isCustom);

    if (!query) return list;
    return list.filter(effect => normalize(effect.name).includes(query) || normalize(effect.description).includes(query));
}

function updateEffectsCounters(countValue) {
    const countMain = document.getElementById('effectsCount');
    const countInline = document.getElementById('effectsCountInline');
    if (countMain) countMain.textContent = String(countValue);
    if (countInline) countInline.textContent = String(countValue);
}

function renderEffects(list) {
    const container = document.getElementById('effectsList');
    updateEffectsCounters(list.length);

    if (!container) return;
    if (!list.length) {
        container.innerHTML = '<div class="col-12"><div class="text-center text-muted py-4 border rounded bg-white">Aucun effet trouvé.</div></div>';
        return;
    }

    container.innerHTML = list.map(effect => `
        <div class="col-md-6 col-xl-4">
            <div class="card h-100 shadow-sm border-start border-5 ${effect.isCustom ? 'border-warning' : 'border-success'}">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h5 class="card-title fw-bold mb-0">${escapeHtml(effect.name)}</h5>
                        <span class="badge ${effect.isCustom ? 'bg-warning text-dark' : 'bg-success'}">
                            ${effect.isCustom ? 'Custom' : 'Système'}
                        </span>
                    </div>
                    <p class="card-text small mb-2">${escapeHtml(effect.description || 'Sans description')}</p>
                    <small class="text-muted d-block">Durée: ${effect.durationHours}h</small>
                    ${renderModifierRow('Growth', effect.growthRateModifier)}
                    ${renderModifierRow('Stress reduction', effect.stressReduction)}
                    ${renderModifierRow('Temp', effect.temperatureModifier)}
                    ${renderModifierRow('Humidité', effect.humidityModifier)}
                    ${renderModifierRow('Lumière', effect.luxModifier)}
                    ${renderModifierRow('Eau', effect.waterModifier)}
                </div>
            </div>
        </div>
    `).join('');
}

async function loadEffects() {
    clearEffectsFeedback();

    const refreshBtn = document.getElementById('effectsRefreshBtn');
    if (refreshBtn) {
        refreshBtn.disabled = true;
        refreshBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Chargement...';
    }

    try {
        effectsCache = await getJson(API_EFFECTS);
        renderEffects(filteredEffects());
        await loadEffectsOpsSnapshot();
    } catch (error) {
        console.error('Erreur loadEffects:', error);
        showEffectsFeedback(`Impossible de charger les effets: ${error.message}`, 'danger');
    } finally {
        if (refreshBtn) {
            refreshBtn.disabled = false;
            refreshBtn.innerHTML = '<i class="fas fa-rotate"></i> Rafraîchir';
        }
    }
}

async function loadEffectsOpsSnapshot() {
    const alerts = document.getElementById('effectsOpsAlerts');
    const readings = document.getElementById('effectsOpsReadings');
    if (!alerts || !readings) return;

    try {
        const overview = await getJson(API_GREENHOUSE_OVERVIEW);
        alerts.textContent = String(overview?.activeAlerts ?? 0);
        readings.textContent = String(overview?.sensorReadings24h ?? 0);
    } catch (_) {
        alerts.textContent = 'N/A';
        readings.textContent = 'N/A';
    }
}

async function createEffect() {
    clearEffectsFeedback();

    const payload = {
        name: document.getElementById('effName').value.trim(),
        description: document.getElementById('effDescription').value.trim(),
        durationHours: Number.parseInt(document.getElementById('effDuration').value, 10),
        growthRateModifier: Number.parseFloat(document.getElementById('effGrowth').value) || 0,
        stressReduction: Number.parseFloat(document.getElementById('effStress').value) || 0,
        temperatureModifier: Number.parseFloat(document.getElementById('effTemp').value) || 0
    };

    if (!payload.name || Number.isNaN(payload.durationHours) || payload.durationHours < 1) {
        showEffectsFeedback('Nom + durée valide sont requis.', 'warning');
        return;
    }

    if (payload.name.length < 3) {
        showEffectsFeedback('Le nom de l\'effet doit contenir au moins 3 caractères.', 'warning');
        return;
    }

    const submitBtn = document.getElementById('effectsCreateBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Création...';
    }

    try {
        await getJson(API_EFFECTS, {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        const modal = bootstrap.Modal.getInstance(document.getElementById('createEffectModal'));
        modal?.hide();
        document.getElementById('effName').value = '';
        document.getElementById('effDescription').value = '';
        showEffectsFeedback('Effet créé avec succès.', 'success');
        await loadEffects();
    } catch (error) {
        console.error('Erreur createEffect:', error);
        showEffectsFeedback(`Création impossible: ${error.message}`, 'danger');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Créer';
        }
    }
}

function onEffectsFilterChanged() {
    renderEffects(filteredEffects());
}
