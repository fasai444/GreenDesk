// --- Guided Tour ultra-light ---
const TOUR_STEPS = [
    {
        el: '#simOnboarding',
        msg: "Bienvenue ! Suivez l'onboarding pour progresser étape par étape.",
    },
    {
        el: '.nav.nav-pills',
        msg: "Naviguez entre les onglets pour explorer forêts, espèces et plantes.",
    },
    {
        el: '#allPlantsTable',
        msg: "Analysez ici toutes vos plantations et leur état en temps réel.",
    },
    {
        el: '#simFeedback',
        msg: "Les retours et alertes s'affichent ici pour vous guider.",
    },
];

function startGuidedTour() {
    if (localStorage.getItem('gdTourDone')) return;
    let step = 0;
    const overlay = document.createElement('div');
    overlay.style.position = 'fixed';
    overlay.style.top = 0;
    overlay.style.left = 0;
    overlay.style.width = '100vw';
    overlay.style.height = '100vh';
    overlay.style.background = 'rgba(15,23,42,0.18)';
    overlay.style.zIndex = 1001;
    overlay.style.pointerEvents = 'auto';
    overlay.style.transition = 'background 0.22s';
    document.body.appendChild(overlay);

    function showStep() {
        // Remove highlight from all
        document.querySelectorAll('.gd-tour-highlight').forEach(e => e.classList.remove('gd-tour-highlight'));
        const s = TOUR_STEPS[step];
        const el = document.querySelector(s.el);
        if (!el) return nextStep();
        el.classList.add('gd-tour-highlight');
        // Tooltip
        let tip = document.getElementById('gdTourTip');
        if (!tip) {
            tip = document.createElement('div');
            tip.id = 'gdTourTip';
            tip.style.position = 'absolute';
            tip.style.zIndex = 1003;
            tip.style.background = '#fff';
            tip.style.border = '1.5px solid #0ea5e9';
            tip.style.borderRadius = '12px';
            tip.style.padding = '1.1em 1.3em';
            tip.style.boxShadow = '0 8px 32px rgba(14,165,233,0.13)';
            tip.style.fontSize = '1.05em';
            tip.style.maxWidth = '340px';
            tip.style.pointerEvents = 'auto';
            tip.style.transition = 'opacity 0.22s';
            tip.innerHTML = '';
            document.body.appendChild(tip);
        }
        tip.innerHTML = `<div style='font-weight:600; margin-bottom:0.4em;'>${s.msg}</div><button id='gdTourNext' class='btn btn-sm btn-info mt-1'>${step < TOUR_STEPS.length-1 ? 'Suivant' : 'Terminer'}</button>`;
        // Position tip
        const rect = el.getBoundingClientRect();
        tip.style.top = (window.scrollY + rect.bottom + 12) + 'px';
        tip.style.left = (window.scrollX + rect.left) + 'px';
        tip.style.opacity = 1;
        // Scroll into view if needed
        el.scrollIntoView({behavior:'smooth', block:'center'});
        document.getElementById('gdTourNext').onclick = nextStep;
    }
    function nextStep() {
        step++;
        if (step >= TOUR_STEPS.length) {
            endTour();
            return;
        }
        showStep();
    }
    function endTour() {
        document.querySelectorAll('.gd-tour-highlight').forEach(e => e.classList.remove('gd-tour-highlight'));
        const tip = document.getElementById('gdTourTip');
        if (tip) tip.remove();
        overlay.remove();
        localStorage.setItem('gdTourDone', '1');
    }
    showStep();
}

window.addEventListener('DOMContentLoaded', () => {
    setTimeout(startGuidedTour, 600);
});
const API = {
    FORESTS: '/api/forests',
    PLANTS: '/plants',
    SPECIES: '/api/species',
    EFFECTS: '/api/effects',
    GREENHOUSE_OVERVIEW: '/api/greenhouse/overview'
};

let currentX = null;
let currentY = null;
let selectedPlantId = null;
let autoPlayInterval = null;
let plantModal = null;
let actionModal = null;
let selectedInspectorAction = null;
let heatmapEnabled = false;
let currentSeasonType = 'SPRING';
let forestSnapshot = null;
let onboardingState = {
    hasSpecies: false,
    hasForest: false,
    hasPlant: false,
    hasEffect: false
};

const DEFAULT_SELECTION_HINT = 'Cliquez une cellule pour la sélectionner, puis utilisez l\'inspecteur à droite pour agir.';

function setButtonBusy(buttonId, busy, busyHtml, idleHtml) {
    const button = q(buttonId);
    if (!button) return;
    button.disabled = busy;
    button.innerHTML = busy ? busyHtml : idleHtml;
}

function q(id) {
    return document.getElementById(id);
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, {
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options
    });

    const contentType = response.headers.get('content-type') || '';
    const hasJson = contentType.includes('application/json');
    const payload = hasJson ? await response.json() : await response.text();

    if (!response.ok) {
        const msg = typeof payload === 'string'
            ? payload
            : payload?.error || payload?.message || `HTTP ${response.status}`;
        throw new Error(msg);
    }

    return payload;
}

function showFeedback(message, variant = 'danger') {
    const feedback = q('simFeedback');
    if (!feedback) return;
    feedback.className = `alert alert-${variant} app-feedback`;
    feedback.textContent = message;
    feedback.classList.remove('d-none');
}

function clearFeedback() {
    const feedback = q('simFeedback');
    if (!feedback) return;
    feedback.classList.add('d-none');
    feedback.textContent = '';
}

function setStepBadge(stepId, state) {
    const badge = q(stepId);
    if (!badge) return;
    badge.classList.remove('step-done', 'step-active');
    if (state === 'done') badge.classList.add('step-done');
    if (state === 'active') badge.classList.add('step-active');
}

function updateOnboardingUI() {
    const completed = [onboardingState.hasSpecies, onboardingState.hasForest, onboardingState.hasPlant, onboardingState.hasEffect]
        .filter(Boolean).length;

    const summary = q('onboardingSummary');
    if (summary) summary.textContent = `Progression: ${completed}/4`;

    setStepBadge('stepSpecies', onboardingState.hasSpecies ? 'done' : 'active');
    setStepBadge('stepForest', onboardingState.hasForest ? 'done' : (onboardingState.hasSpecies ? 'active' : 'idle'));
    setStepBadge('stepPlant', onboardingState.hasPlant ? 'done' : (onboardingState.hasForest ? 'active' : 'idle'));
    setStepBadge('stepEffect', onboardingState.hasEffect ? 'done' : (onboardingState.hasPlant ? 'active' : 'idle'));
}

function sanitizeState(state) {
    return (state || 'HEALTHY').toUpperCase();
}

function stateLabel(state) {
    const value = sanitizeState(state);
    if (value === 'HEALTHY') return 'HEALTHY';
    if (value === 'STRESSED') return 'STRESSED';
    if (value === 'DISEASED') return 'DISEASED';
    if (value === 'DORMANT') return 'DORMANT';
    return 'UNKNOWN';
}

function normalizeSeasonType(value) {
    if (!value) return 'SPRING';
    const raw = String(value).trim().toUpperCase();
    if (['WINTER', 'SPRING', 'SUMMER', 'AUTUMN'].includes(raw)) return raw;
    if (raw.includes('HIVER')) return 'WINTER';
    if (raw.includes('PRINTEMPS')) return 'SPRING';
    if (raw.includes('ÉTÉ') || raw.includes('ETE')) return 'SUMMER';
    if (raw.includes('AUTOMNE')) return 'AUTUMN';
    return 'SPRING';
}

async function loadSimulationOpsSnapshot() {
    const alerts = q('simOpsAlerts');
    const effects = q('simOpsEffects');
    const readings = q('simOpsReadings');
    if (!alerts || !effects || !readings) return;

    try {
        const overview = await fetchJson(API.GREENHOUSE_OVERVIEW);
        alerts.textContent = String(overview?.activeAlerts ?? 0);
        effects.textContent = String(overview?.activeEffects ?? 0);
        readings.textContent = String(overview?.sensorReadings24h ?? 0);
    } catch (_) {
        alerts.textContent = 'N/A';
        effects.textContent = 'N/A';
        readings.textContent = 'N/A';
    }
}

function stressToHeatColor(stressLevel) {
    const safeValue = Math.max(0, Math.min(1, stressLevel || 0));
    const hue = 120 - Math.round(safeValue * 120);
    return `hsl(${hue}, 85%, 50%)`;
}

function renderWeatherLayer(seasonType) {
    const layer = q('weatherLayer');
    const hint = q('seasonVisualHint');
    const shell = document.querySelector('.forest-scene-shell');
    if (!layer || !hint || !shell) return;

    const season = normalizeSeasonType(seasonType);
    currentSeasonType = season;
    hint.textContent = `Mode: ${season}`;

    shell.classList.remove('season-winter', 'season-spring', 'season-summer', 'season-autumn');
    shell.classList.add(`season-${season.toLowerCase()}`);

    layer.className = `weather-layer weather-${season.toLowerCase()}`;
    layer.innerHTML = '';

    const particleCount = season === 'WINTER' ? 26 : season === 'AUTUMN' ? 18 : season === 'SPRING' ? 14 : 10;
    for (let index = 0; index < particleCount; index += 1) {
        const particle = document.createElement('span');
        particle.className = 'weather-particle';
        particle.style.setProperty('--left', `${Math.random() * 100}%`);
        particle.style.setProperty('--delay', `${Math.random() * 2.2}s`);
        particle.style.setProperty('--duration', `${2.8 + Math.random() * 2.8}s`);
        layer.appendChild(particle);
    }
}

function speciesHue(speciesName = '') {
    const name = String(speciesName || 'default');
    let hash = 0;
    for (let index = 0; index < name.length; index += 1) {
        hash = ((hash << 5) - hash) + name.charCodeAt(index);
        hash |= 0;
    }
    return Math.abs(hash) % 120;
}

function speciesScale(speciesName = '') {
    const value = speciesHue(speciesName);
    return 0.92 + (value % 15) / 100;
}

function buildTreeMarkup(plant) {
    const hue = speciesHue(plant.species?.name);
    const scale = speciesScale(plant.species?.name);
    const seasonalTint =
        currentSeasonType === 'AUTUMN' ? -18
            : currentSeasonType === 'WINTER' ? -28
                : currentSeasonType === 'SUMMER' ? 10
                    : 0;

    const state = sanitizeState(plant.plantState);
    const stateClass = `tree-${state.toLowerCase()}`;

    return `
        <div class="tree-visual ${stateClass}" style="--tree-hue:${hue}; --tree-scale:${scale}; --seasonal-tint:${seasonalTint};">
            <svg class="tree-svg" viewBox="0 0 100 100" role="img" aria-hidden="true" focusable="false">
                <defs>
                    <linearGradient id="trunkGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stop-color="#9a6b45"></stop>
                        <stop offset="100%" stop-color="#5f3f29"></stop>
                    </linearGradient>
                    <radialGradient id="leafMain" cx="35%" cy="28%" r="70%">
                        <stop offset="0%" stop-color="hsl(calc(120 + var(--tree-hue) + var(--seasonal-tint)), 62%, 48%)"></stop>
                        <stop offset="100%" stop-color="hsl(calc(100 + var(--tree-hue) + var(--seasonal-tint)), 56%, 29%)"></stop>
                    </radialGradient>
                    <radialGradient id="leafAlt" cx="45%" cy="38%" r="66%">
                        <stop offset="0%" stop-color="hsl(calc(110 + var(--tree-hue) + var(--seasonal-tint)), 56%, 44%)"></stop>
                        <stop offset="100%" stop-color="hsl(calc(92 + var(--tree-hue) + var(--seasonal-tint)), 52%, 27%)"></stop>
                    </radialGradient>
                </defs>

                <ellipse class="tree-shadow" cx="50" cy="88" rx="25" ry="8"></ellipse>
                <path class="tree-trunk-shape" d="M46 42 C44 60, 44 74, 46 90 L56 90 C58 74, 58 60, 56 42 Z" fill="url(#trunkGradient)"></path>
                <path class="tree-branch" d="M48 58 C40 54, 36 49, 34 43"/>
                <path class="tree-branch" d="M54 60 C62 54, 66 50, 68 44"/>

                <ellipse class="tree-canopy-main" cx="50" cy="38" rx="30" ry="23" fill="url(#leafMain)"></ellipse>
                <ellipse class="tree-canopy-mid" cx="36" cy="42" rx="18" ry="15" fill="url(#leafAlt)"></ellipse>
                <ellipse class="tree-canopy-mid" cx="64" cy="40" rx="16" ry="14" fill="url(#leafAlt)"></ellipse>
                <ellipse class="tree-canopy-top" cx="50" cy="22" rx="18" ry="12" fill="url(#leafMain)"></ellipse>
                <circle class="tree-highlight" cx="42" cy="26" r="8"></circle>
            </svg>
        </div>
    `;
}

function updateForestMetrics(forest, plants) {
    const totalCells = (forest?.width || 0) * (forest?.height || 0);
    const totalPlants = plants.length;
    const healthy = plants.filter(p => sanitizeState(p.plantState) === 'HEALTHY').length;
    const stressed = plants.filter(p => sanitizeState(p.plantState) === 'STRESSED').length;
    const diseased = plants.filter(p => sanitizeState(p.plantState) === 'DISEASED').length;

    const coverage = totalCells > 0 ? Math.round((totalPlants / totalCells) * 100) : 0;

    let healthLabel = 'N/A';
    if (totalPlants > 0) {
        if (diseased > 0) healthLabel = `${healthy}/${totalPlants} sains`;
        else if (stressed > 0) healthLabel = `${healthy}/${totalPlants} stables`;
        else healthLabel = 'Excellent';
    }

    q('forestStatsCells').textContent = String(totalCells);
    q('forestStatsPlants').textContent = String(totalPlants);
    q('forestStatsCoverage').textContent = `${coverage}%`;
    q('forestStatsHealth').textContent = healthLabel;
}

function setInspector(plant, x, y) {
    const empty = q('cellInspectorEmpty');
    const content = q('cellInspectorContent');
    const actionBtn = q('inspectorActionBtn');
    const selectionHint = q('selectionHint');

    if (!empty || !content || !actionBtn) return;

    if (!plant) {
        const hasSelectedCell = Number.isInteger(x) && Number.isInteger(y);

        if (!hasSelectedCell) {
            empty.classList.remove('d-none');
            content.classList.add('d-none');
            if (selectionHint) {
                selectionHint.textContent = DEFAULT_SELECTION_HINT;
            }
            selectedInspectorAction = null;
            return;
        }

        empty.classList.add('d-none');
        content.classList.remove('d-none');
        q('inspectorPosition').textContent = `(${x}, ${y})`;
        q('inspectorPlantName').textContent = 'Case vide';
        q('inspectorSpecies').textContent = '-';
        q('inspectorState').textContent = '-';
        q('inspectorStress').textContent = '-';
        if (selectionHint) {
            selectionHint.textContent = `Cellule (${x}, ${y}) sélectionnée. Case vide prête pour plantation.`;
        }
        actionBtn.classList.remove('btn-outline-success');
        actionBtn.classList.add('btn-success');
        actionBtn.textContent = 'Créer une plante ici';
        selectedInspectorAction = () => openPlantModal(x, y);
        return;
    }

    empty.classList.add('d-none');
    content.classList.remove('d-none');
    q('inspectorPosition').textContent = `(${x}, ${y})`;
    q('inspectorPlantName').textContent = plant.name || 'N/A';
    q('inspectorSpecies').textContent = plant.species?.name || 'N/A';
    q('inspectorState').textContent = stateLabel(plant.plantState);
    q('inspectorStress').textContent = `${((plant.stressIndex || 0) * 100).toFixed(1)}%`;
    if (selectionHint) {
        selectionHint.textContent = `Cellule (${x}, ${y}) sélectionnée. Plante active: ${plant.name || 'N/A'}.`;
    }
    actionBtn.classList.remove('btn-success');
    actionBtn.classList.add('btn-outline-success');
    actionBtn.textContent = 'Ouvrir les actions de la plante';
    selectedInspectorAction = () => openPlantActionModal(plant);
}

document.addEventListener('DOMContentLoaded', async () => {
    plantModal = new bootstrap.Modal(q('plantSeedModal'));
    actionModal = new bootstrap.Modal(q('plantActionModal'));

    q('inspectorActionBtn')?.addEventListener('click', () => {
        if (typeof selectedInspectorAction === 'function') {
            selectedInspectorAction();
        }
    });

    q('btnHeatmap')?.addEventListener('click', () => {
        heatmapEnabled = !heatmapEnabled;
        const button = q('btnHeatmap');
        button?.classList.toggle('btn-dark', heatmapEnabled);
        button?.classList.toggle('btn-outline-dark', !heatmapEnabled);
        if (forestSnapshot) {
            drawGrid(forestSnapshot.forest, forestSnapshot.plants);
        }
    });

    try {
        await loadForests();
        await loadSpeciesForModal();
        await loadSpecies();
        await loadAllPlants();
        await loadSimulationOpsSnapshot();
        updateOnboardingUI();
    } catch (error) {
        showFeedback(`Erreur au chargement initial: ${error.message}`);
    }
});

async function loadForests(selectId = null) {
    clearFeedback();
    const forests = await fetchJson(API.FORESTS);
    const select = q('forestSelect');
    onboardingState.hasForest = forests.length > 0;
    updateOnboardingUI();

    if (!select) return;

    select.innerHTML = '<option value="" selected disabled>-- Choisir une forêt --</option>';
    forests.forEach(forest => {
        const option = document.createElement('option');
        option.value = forest.id;
        option.textContent = `${forest.name} (${forest.width} × ${forest.height})`;
        select.appendChild(option);
    });

    if (selectId && forests.some(f => f.id === selectId)) {
        select.value = selectId;
        await loadForestDetails();
    } else {
        const current = select.value;
        if (current) {
            await loadForestDetails();
        } else {
            q('gridContainer').innerHTML = '';
            q('forestEmptyState')?.classList.remove('d-none');
        }
    }
}

async function createForest() {
    const name = q('newForestName')?.value?.trim();
    const width = Number.parseInt(q('newForestW')?.value, 10);
    const height = Number.parseInt(q('newForestH')?.value, 10);

    if (!name || Number.isNaN(width) || Number.isNaN(height) || width < 3 || height < 3) {
        alert('Nom obligatoire + dimensions >= 3');
        return;
    }

    try {
        setButtonBusy('createForestBtn', true, '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Création...', 'Créer et ouvrir');
        const forest = await fetchJson(API.FORESTS, {
            method: 'POST',
            body: JSON.stringify({ name, width, height })
        });

        bootstrap.Modal.getInstance(q('createForestModal'))?.hide();
        q('newForestName').value = '';
        await loadForests(forest.id);
        showFeedback(`Étape 2 validée: forêt "${forest.name}" créée et ouverte.`, 'success');
    } catch (error) {
        showFeedback(`Création impossible: ${error.message}`);
    } finally {
        setButtonBusy('createForestBtn', false, '', 'Créer et ouvrir');
    }
}

async function deleteForest() {
    const forestId = q('forestSelect')?.value;
    if (!forestId) return;
    if (!confirm('Supprimer cette forêt et ses positions ?')) return;

    try {
        await fetchJson(`${API.FORESTS}/${forestId}`, { method: 'DELETE' });
        q('gridContainer').innerHTML = '';
        q('forestEmptyState')?.classList.remove('d-none');
        q('forestTitle').textContent = 'Aucune forêt sélectionnée';
        q('seasonPanel')?.classList.add('d-none');
        updateForestMetrics({ width: 0, height: 0 }, []);
        setInspector(null, '-', '-');
        await loadForests();
        await loadAllPlants();
        showFeedback('Forêt supprimée.', 'success');
    } catch (error) {
        showFeedback(`Suppression impossible: ${error.message}`);
    }
}

async function resetCurrentForestPlants() {
    const forestId = q('forestSelect')?.value;
    if (!forestId) {
        alert('Sélectionnez une forêt.');
        return;
    }

    if (!confirm('Supprimer toutes les plantes de cette forêt ?')) return;

    const plants = await fetchJson(`${API.FORESTS}/${forestId}/plants`);
    for (const plant of plants) {
        await fetchJson(`${API.PLANTS}/${plant.id}`, { method: 'DELETE' });
    }

    await loadForestDetails();
    await loadAllPlants();
    alert('Forêt réinitialisée.');
}

async function ensureSeasonCycle(forestId) {
    try {
        await fetchJson(`${API.FORESTS}/${forestId}/season-cycle`);
    } catch {
        await fetchJson(`${API.FORESTS}/${forestId}/season-cycle`, { method: 'POST' });
    }
}

async function loadSeason(forestId) {
    await ensureSeasonCycle(forestId);

    const data = await fetchJson(`${API.FORESTS}/${forestId}/season-cycle`);
    const seasonType = normalizeSeasonType(data?.currentSeasonData?.type || data?.cycle?.currentSeason || data?.currentSeasonData?.name);
    const seasonName = data?.currentSeasonData?.name || seasonType;

    const panel = q('seasonPanel');
    const label = q('currentSeasonName');
    if (!panel || !label) return;

    panel.classList.remove('d-none');
    label.textContent = seasonName;
    renderWeatherLayer(seasonType);
}

async function advanceSeason() {
    const forestId = q('forestSelect')?.value;
    if (!forestId) {
        alert('Sélectionnez une forêt.');
        return;
    }

    await ensureSeasonCycle(forestId);
    await fetchJson(`${API.FORESTS}/${forestId}/season-cycle/advance`, {
        method: 'POST',
        body: JSON.stringify({ monthsElapsed: 3 })
    });

    await loadForestDetails();
    await loadAllPlants();
}

function toggleAutoPlay() {
    const btn = q('btnAutoPlay');
    const forestId = q('forestSelect')?.value;

    if (!forestId) {
        alert('Sélectionnez une forêt d’abord.');
        return;
    }

    if (autoPlayInterval) {
        clearInterval(autoPlayInterval);
        autoPlayInterval = null;
        btn.innerHTML = '<i class="fas fa-play"></i> Auto';
        btn.classList.remove('btn-danger');
        btn.classList.add('btn-outline-danger');
        return;
    }

    btn.innerHTML = '<i class="fas fa-stop"></i> Stop';
    btn.classList.remove('btn-outline-danger');
    btn.classList.add('btn-danger');

    advanceSeason();
    autoPlayInterval = setInterval(advanceSeason, 2500);
}

async function loadForestDetails() {
    const forestId = q('forestSelect')?.value;
    if (!forestId) return;

    try {
        const forest = await fetchJson(`${API.FORESTS}/${forestId}`);
        const plants = await fetchJson(`${API.FORESTS}/${forestId}/plants`);

        await loadSeason(forestId);

        q('forestTitle').textContent = `${forest.name} — ${plants.length} arbre(s)`;
        q('forestEmptyState')?.classList.add('d-none');
        q('gridLegend')?.classList.remove('d-none');
        updateForestMetrics(forest, plants);
        forestSnapshot = { forest, plants };
        onboardingState.hasPlant = plants.length > 0;
        updateOnboardingUI();
        drawGrid(forest, plants);
        await loadSimulationOpsSnapshot();
    } catch (error) {
        showFeedback(`Impossible de charger la forêt: ${error.message}`);
    }
}

function drawGrid(forest, plants) {
    const container = q('gridContainer');
    if (!container) return;

    container.innerHTML = '';
    const grid = document.createElement('div');
    grid.className = 'forest-grid';
    grid.style.gridTemplateColumns = `repeat(${forest.width}, minmax(34px, 1fr))`;
    grid.style.setProperty('--forest-cols', String(forest.width));

    for (let y = 0; y < forest.height; y++) {
        for (let x = 0; x < forest.width; x++) {
            const cell = document.createElement('div');
            cell.className = 'forest-cell';

            const plant = plants.find(p => p.x === x && p.y === y);

            if (plant) {
                const state = sanitizeState(plant.plantState);
                cell.classList.add('occupied', `status-${state}`);
                cell.innerHTML = buildTreeMarkup(plant);
                cell.title = `${plant.name}\n${plant.species?.name || '?'}\n${stateLabel(state)}`;
                const stress = Math.max(0, Math.min(1, plant.stressIndex || 0));
                cell.style.setProperty('--stress-color', stressToHeatColor(stress));
                cell.style.setProperty('--stress-alpha', (0.12 + stress * 0.42).toFixed(2));
                if (heatmapEnabled) {
                    cell.classList.add('heatmap-enabled');
                }
                cell.onclick = () => {
                    setInspector(plant, x, y);
                };
            } else {
                cell.classList.add('cell-empty');
                cell.innerHTML = '<span class="grass-tuft" aria-hidden="true"></span>';
                cell.title = `Case vide (${x},${y})`;
                cell.onclick = () => {
                    setInspector(null, x, y);
                };
            }

            grid.appendChild(cell);
        }
    }

    container.appendChild(grid);
    setInspector(null, '-', '-');
}

async function loadSpeciesForModal() {
    const species = await fetchJson(API.SPECIES);
    const select = q('speciesSelectModal');
    if (!select) return;

    select.innerHTML = '<option value="" selected disabled>Choisir une espèce...</option>';
    species.forEach(s => {
        const option = document.createElement('option');
        option.value = s.id;
        option.textContent = `${s.name} (eau optimale: ${s.optimalWaterNeeds})`;
        select.appendChild(option);
    });
}

function openPlantModal(x, y) {
    currentX = x;
    currentY = y;
    q('targetX').textContent = String(x);
    q('targetY').textContent = String(y);
    q('plantNameInput').value = `Plante_${x}_${y}`;
    plantModal.show();
}

async function confirmPlanting() {
    const forestId = q('forestSelect')?.value;
    const speciesId = q('speciesSelectModal')?.value;
    const name = q('plantNameInput')?.value?.trim();

    if (!forestId || !speciesId || !name) {
        alert('Tous les champs sont requis.');
        return;
    }

    try {
        setButtonBusy('plantConfirmBtn', true, '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Plantation...', 'Créer et planter');
        const plant = await fetchJson(`${API.PLANTS}/create?name=${encodeURIComponent(name)}&speciesId=${encodeURIComponent(speciesId)}`, {
            method: 'POST'
        });

        try {
            await fetchJson(`${API.FORESTS}/${forestId}/plants`, {
                method: 'POST',
                body: JSON.stringify({ plantId: plant.id, x: currentX, y: currentY })
            });
        } catch (error) {
            await fetchJson(`${API.PLANTS}/${plant.id}`, { method: 'DELETE' }).catch(() => undefined);
            throw error;
        }

        plantModal.hide();
        await loadForestDetails();
        await loadAllPlants();
        onboardingState.hasPlant = true;
        updateOnboardingUI();
        showFeedback('Étape 3 validée: plante créée et positionnée.', 'success');
    } catch (error) {
        showFeedback(`Plantation impossible: ${error.message}`);
    } finally {
        setButtonBusy('plantConfirmBtn', false, '', 'Créer et planter');
    }
}

async function openPlantActionModal(plant) {
    selectedPlantId = plant.id;

    q('plantActionTitle').textContent = plant.name;
    q('pSpecies').textContent = plant.species?.name || 'N/A';
    q('pState').textContent = stateLabel(plant.plantState);
    q('pStress').textContent = `${((plant.stressIndex || 0) * 100).toFixed(1)}%`;

    const effects = await fetchJson(API.EFFECTS);
    const select = q('effectSelector');
    select.innerHTML = '<option selected disabled>Choisir un effet...</option>';
    effects.forEach(effect => {
        const option = document.createElement('option');
        option.value = effect.id;
        option.textContent = `${effect.name} (${effect.durationHours}h)`;
        select.appendChild(option);
    });

    const active = await fetchJson(`/api/plants/${selectedPlantId}/effects/active`);
    const list = q('activeEffectsList');
    list.innerHTML = '';

    if (!active.length) {
        list.innerHTML = '<li class="list-group-item text-muted">Aucun effet actif</li>';
    } else {
        active.forEach(item => {
            const li = document.createElement('li');
            li.className = 'list-group-item';
            li.textContent = item.name || item.effectId;
            list.appendChild(li);
        });
    }

    onboardingState.hasEffect = active.length > 0 || onboardingState.hasEffect;
    updateOnboardingUI();

    actionModal.show();
}

async function applyEffectToPlant() {
    const effectId = q('effectSelector')?.value;
    if (!effectId || !selectedPlantId) return;

    try {
        setButtonBusy('applyEffectBtn', true, '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Application...', 'Appliquer');
        await fetchJson(`/api/plants/${selectedPlantId}/effects/${effectId}`, { method: 'POST' });
        actionModal.hide();
        await loadForestDetails();
        onboardingState.hasEffect = true;
        updateOnboardingUI();
        showFeedback('Étape 4 validée: effet appliqué avec succès.', 'success');
    } catch (error) {
        showFeedback(`Application de l'effet impossible: ${error.message}`);
    } finally {
        setButtonBusy('applyEffectBtn', false, '', 'Appliquer');
    }
}

async function deletePlantFromForest() {
    if (!selectedPlantId) return;
    if (!confirm('Arracher cette plante ?')) return;

    try {
        setButtonBusy('deletePlantBtn', true, '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Suppression...', '<i class="fas fa-trash"></i> Arracher la plante');
        await fetchJson(`${API.PLANTS}/${selectedPlantId}`, { method: 'DELETE' });
        actionModal.hide();
        await loadForestDetails();
        await loadAllPlants();
        showFeedback('Arbre supprimé.', 'success');
    } catch (error) {
        showFeedback(`Suppression impossible: ${error.message}`);
    } finally {
        setButtonBusy('deletePlantBtn', false, '', '<i class="fas fa-trash"></i> Arracher la plante');
    }
}

async function loadSpecies() {
    const list = await fetchJson(API.SPECIES);
    const container = q('speciesList');
    if (!container) return;

    onboardingState.hasSpecies = list.length > 0;
    updateOnboardingUI();

    if (!list.length) {
        container.innerHTML = `
            <div class="col-12">
                <div class="sim-empty-state">
                    <h6 class="mb-1">Aucune espèce disponible</h6>
                    <p class="small text-muted mb-2">Commencez par créer des espèces pour lancer un scénario fiable.</p>
                    <a class="btn btn-sm btn-outline-success" href="species.html">Créer des espèces</a>
                </div>
            </div>
        `;
        return;
    }

    container.innerHTML = list.map(species => `
        <div class="col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h6 class="fw-bold mb-2">${escapeHtml(species.name)}</h6>
                    <small class="text-muted d-block">Eau: ${species.optimalWaterNeeds}</small>
                    <small class="text-muted d-block">Temp: ${species.optimalTemperature}°C</small>
                    <small class="text-muted d-block">Humidité: ${species.optimalHumidity}%</small>
                    <small class="text-muted d-block">Lumière: ${species.optimalLuxNeeds} lux</small>
                </div>
            </div>
        </div>
    `).join('');
}

async function loadAllPlants() {
    const plants = await fetchJson(API.PLANTS);
    const tbody = q('allPlantsTable');
    if (!tbody) return;

    if (!plants.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">Aucune plante enregistrée.</td></tr>';
        onboardingState.hasPlant = false;
        updateOnboardingUI();
        return;
    }

    onboardingState.hasPlant = true;
    updateOnboardingUI();

    tbody.innerHTML = plants
        .slice()
        .sort((left, right) => String(left.name || '').localeCompare(String(right.name || '')))
        .map(plant => `
        <tr>
            <td class="fw-semibold">${escapeHtml(plant.name)}</td>
            <td>${escapeHtml(plant.species?.name || 'N/A')}</td>
            <td><span class="badge bg-secondary">${stateLabel(plant.plantState)}</span></td>
            <td>${((plant.stressIndex || 0) * 100).toFixed(1)}%</td>
            <td>${plant.forestId ? '<span class="badge bg-light text-dark border">Affectée</span>' : '<span class="badge bg-light text-muted border">Hors forêt</span>'}</td>
        </tr>
    `).join('');
}
