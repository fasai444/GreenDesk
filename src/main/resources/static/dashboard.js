const URLS = {
    SPECIES: '/api/species',
    PLANTS: '/plants',
    FORESTS: '/api/forests',
    STIMULI: '/api/stimuli',
    GREENHOUSE: '/api/greenhouse'
};

let cachedPlants = [];
let sensorStreamInterval = null;
let emittedSensorCount = 0;
let forestRoiCache = [];

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function showDashboardFeedback(message, variant = 'info') {
    const feedback = document.getElementById('dashboardFeedback');
    if (!feedback) return;
    feedback.className = `alert alert-${variant} app-feedback mb-3`;
    feedback.textContent = message;
    feedback.classList.remove('d-none');
}

function clearDashboardFeedback() {
    const feedback = document.getElementById('dashboardFeedback');
    if (!feedback) return;
    feedback.textContent = '';
    feedback.classList.add('d-none');
}

function badgeClassForState(state) {
    const s = (state || '').toUpperCase();
    if (s === 'HEALTHY') return 'bg-success';
    if (s === 'STRESSED') return 'bg-warning text-dark';
    if (s === 'DISEASED') return 'bg-danger';
    if (s === 'DORMANT') return 'bg-secondary';
    return 'bg-light text-dark border';
}

function dominantStateBadge(plantsInForest) {
    if (!plantsInForest.length) return '<span class="badge bg-light text-dark border">N/A</span>';
    const buckets = { HEALTHY: 0, STRESSED: 0, DISEASED: 0, DORMANT: 0 };
    plantsInForest.forEach(plant => {
        const key = String(plant?.plantState || '').toUpperCase();
        if (buckets[key] !== undefined) buckets[key] += 1;
    });

    const winner = Object.entries(buckets).sort((a, b) => b[1] - a[1])[0]?.[0] || 'N/A';
    return `<span class="badge ${badgeClassForState(winner)}">${winner}</span>`;
}

async function getJson(url, options = {}) {
    const response = await fetch(url, {
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options
    });

    const contentType = response.headers.get('content-type') || '';
    const isJson = contentType.includes('application/json');
    const payload = isJson ? await response.json() : await response.text();

    if (!response.ok) {
        const msg = typeof payload === 'string' ? payload : payload?.error || payload?.message || `HTTP ${response.status}`;
        throw new Error(msg);
    }

    return payload;
}

function applyOverviewKpis(overview = {}) {
    document.getElementById('activeEffectsKpi').textContent = String(overview.activeEffects ?? 0);
    document.getElementById('activeAlertsKpi').textContent = String(overview.activeAlerts ?? 0);
    document.getElementById('sensorReadings24h').textContent = String(overview.sensorReadings24h ?? 0);
    if (typeof overview.avgStressPct === 'number') {
        document.getElementById('avgStress').textContent = `${Math.round(overview.avgStressPct)}%`;
    }
}

async function loadRoiInsights() {
    const water = document.getElementById('roiWaterSavings');
    const risk = document.getElementById('roiRiskIndex');
    const cost = document.getElementById('roiDailyCost');
    const recommendation = document.getElementById('roiRecommendation');
    if (!water || !risk || !cost || !recommendation) return;

    try {
        const hours = Number(document.getElementById('roiWindowSelect')?.value || 24);
        const roi = await getJson(`${URLS.GREENHOUSE}/roi?hours=${encodeURIComponent(hours)}`);
        water.textContent = `${Number(roi.estimatedWaterSavingsLiters || 0).toFixed(1)} L`;
        risk.textContent = `${Number(roi.riskIndex || 0).toFixed(1)} / 100`;
        cost.textContent = `${Number(roi.estimatedDailyCostEUR || 0).toFixed(2)} €`;
        recommendation.textContent = String(roi.recommendation || 'Aucune recommandation.');
    } catch (error) {
        water.textContent = 'N/A';
        risk.textContent = 'N/A';
        cost.textContent = 'N/A';
        recommendation.textContent = `ROI indisponible: ${error.message}`;
    }
}

async function loadForestRoiRanking() {
    const table = document.getElementById('forestRoiTable');
    if (!table) return;

    try {
        const hours = Number(document.getElementById('roiWindowSelect')?.value || 24);
        const [ranking, roi24, roi7d, roi30d] = await Promise.all([
            getJson(`${URLS.GREENHOUSE}/roi/forests?limit=12&hours=${encodeURIComponent(hours)}`),
            getJson(`${URLS.GREENHOUSE}/roi/forests?limit=50&hours=24`),
            getJson(`${URLS.GREENHOUSE}/roi/forests?limit=50&hours=168`),
            getJson(`${URLS.GREENHOUSE}/roi/forests?limit=50&hours=720`)
        ]);
        forestRoiCache = ranking;
        if (!ranking.length) {
            table.innerHTML = '<tr><td colspan="6" class="text-muted">Aucune forêt disponible.</td></tr>';
            return;
        }

        const levelBadge = level => {
            if (level === 'RENTABLE') return 'bg-success';
            if (level === 'STABLE') return 'bg-secondary';
            return 'bg-danger';
        };

        const byForest24 = new Map((roi24 || []).map(item => [item.forestId, Number(item.roiScore || 0)]));
        const byForest7d = new Map((roi7d || []).map(item => [item.forestId, Number(item.roiScore || 0)]));
        const byForest30d = new Map((roi30d || []).map(item => [item.forestId, Number(item.roiScore || 0)]));

        const sparkline = values => {
            const ticks = '▁▂▃▄▅▆▇█';
            const min = Math.min(...values);
            const max = Math.max(...values);
            if (max === min) return '▅▅▅';
            return values
                .map(value => {
                    const normalized = (value - min) / (max - min);
                    const index = Math.max(0, Math.min(ticks.length - 1, Math.round(normalized * (ticks.length - 1))));
                    return ticks[index];
                })
                .join('');
        };

        const trendMeta = values => {
            const [v24h, v7d, v30d] = values;
            const shortDelta = v24h - v7d;
            const longDelta = v24h - v30d;

            if (shortDelta <= -8 && longDelta <= -10) {
                return {
                    colorClass: 'text-danger',
                    iconClass: 'fas fa-arrow-trend-down',
                    badgeClass: 'bg-danger',
                    badgeText: 'Alerte baisse',
                    title: `Dégradation marquée (${shortDelta.toFixed(1)} / ${longDelta.toFixed(1)})`
                };
            }

            if (shortDelta >= 8 && longDelta >= 10) {
                return {
                    colorClass: 'text-success',
                    iconClass: 'fas fa-arrow-trend-up',
                    badgeClass: 'bg-success',
                    badgeText: 'Hausse',
                    title: `Amélioration nette (+${shortDelta.toFixed(1)} / +${longDelta.toFixed(1)})`
                };
            }

            if (Math.abs(shortDelta) <= 2 && Math.abs(longDelta) <= 2) {
                return {
                    colorClass: 'text-secondary',
                    iconClass: 'fas fa-minus',
                    badgeClass: 'bg-secondary',
                    badgeText: 'Stable',
                    title: `Évolution stable (${shortDelta.toFixed(1)} / ${longDelta.toFixed(1)})`
                };
            }

            if (shortDelta < 0 || longDelta < 0) {
                return {
                    colorClass: 'text-warning',
                    iconClass: 'fas fa-arrow-trend-down',
                    badgeClass: 'bg-warning text-dark',
                    badgeText: 'À surveiller',
                    title: `Légère baisse (${shortDelta.toFixed(1)} / ${longDelta.toFixed(1)})`
                };
            }

            return {
                colorClass: 'text-success',
                iconClass: 'fas fa-arrow-trend-up',
                badgeClass: 'bg-success',
                badgeText: 'Positive',
                title: `Tendance positive (+${shortDelta.toFixed(1)} / +${longDelta.toFixed(1)})`
            };
        };

        table.innerHTML = ranking.map(item => `
            ${(() => {
                const trendValues = [
                    byForest24.get(item.forestId) ?? Number(item.roiScore || 0),
                    byForest7d.get(item.forestId) ?? Number(item.roiScore || 0),
                    byForest30d.get(item.forestId) ?? Number(item.roiScore || 0)
                ];
                const trend = trendMeta(trendValues);
                return `
            <tr>
                <td class="fw-semibold">${escapeHtml(item.forestName || 'N/A')}</td>
                <td><span class="badge ${levelBadge(item.level)}">${escapeHtml(item.level || 'N/A')}</span></td>
                <td>${Number(item.roiScore || 0).toFixed(1)}</td>
                <td>
                    <span class="${trend.colorClass} fw-semibold" title="${escapeHtml(trend.title)}">
                        <i class="${trend.iconClass} me-1"></i>${sparkline(trendValues)}
                    </span>
                    <span class="badge ${trend.badgeClass} ms-1">${trend.badgeText}</span>
                </td>
                <td>${Number(item.riskIndex || 0).toFixed(1)}</td>
                <td>${Number(item.estimatedDailyCostEUR || 0).toFixed(2)} €</td>
            </tr>
                `;
            })()}
        `).join('');
    } catch (error) {
        forestRoiCache = [];
        table.innerHTML = `<tr><td colspan="6" class="text-danger">${escapeHtml(error.message || 'Erreur de chargement')}</td></tr>`;
    }
}

async function loadRoiPanels() {
    await loadRoiInsights();
    await loadForestRoiRanking();
}

function exportForestRoiCsv() {
    if (!forestRoiCache.length) {
        showDashboardFeedback('Aucune donnée ROI forêt à exporter.', 'warning');
        return;
    }

    const header = ['forestId', 'forestName', 'level', 'roiScore', 'riskIndex', 'estimatedDailyCostEUR', 'estimatedWaterSavingsLiters', 'plants', 'activeAlerts', 'activeEffects', 'windowHours'];
    const rows = forestRoiCache.map(item => [
        item.forestId,
        item.forestName,
        item.level,
        item.roiScore,
        item.riskIndex,
        item.estimatedDailyCostEUR,
        item.estimatedWaterSavingsLiters,
        item.plants,
        item.activeAlerts,
        item.activeEffects,
        item.windowHours
    ]);

    const escapeCsv = value => {
        const raw = String(value ?? '');
        if (/[",\n]/.test(raw)) {
            return `"${raw.replaceAll('"', '""')}"`;
        }
        return raw;
    };

    const csvContent = [header, ...rows]
        .map(line => line.map(escapeCsv).join(','))
        .join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    const stamp = new Date().toISOString().slice(0, 19).replaceAll(':', '-');
    link.href = url;
    link.download = `greenhouse-roi-forests-${stamp}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    showDashboardFeedback('Export CSV ROI forêts généré.', 'success');
}

async function loadDashboardData() {
    const refreshButton = document.getElementById('refreshDashboardBtn');
    if (refreshButton) {
        refreshButton.disabled = true;
        refreshButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Chargement...';
    }

    clearDashboardFeedback();

    try {
        const [species, plants, forests, overview] = await Promise.all([
            getJson(URLS.SPECIES),
            getJson(URLS.PLANTS),
            getJson(URLS.FORESTS),
            getJson(`${URLS.GREENHOUSE}/overview`)
        ]);

        cachedPlants = plants;

        document.getElementById('totalSpecies').textContent = species.length;
        document.getElementById('totalPlants').textContent = plants.length;
        document.getElementById('totalForests').textContent = forests.length;
        applyOverviewKpis(overview);

        const forestsTbody = document.getElementById('forestStatusList');
        const rows = forests.map(forest => {
            const plantsInForest = plants.filter(plant => plant.forestId === forest.id);
            const count = plantsInForest.length;
            const totalCells = Math.max(1, (forest.width || 0) * (forest.height || 0));
            const occupancy = Math.min(100, Math.round((count / totalCells) * 100));

            return {
                count,
                html: `
                    <tr>
                        <td class="fw-semibold">${escapeHtml(forest.name)}</td>
                        <td>${forest.width} × ${forest.height}</td>
                        <td>${count}</td>
                        <td>${occupancy}%</td>
                        <td>${dominantStateBadge(plantsInForest)}</td>
                    </tr>
                `
            };
        }).sort((left, right) => right.count - left.count);

        forestsTbody.innerHTML = rows.map(row => row.html).join('') || '<tr><td colspan="5" class="text-muted">Aucune forêt</td></tr>';

        const recent = [...plants]
            .sort((left, right) => String(right.id || '').localeCompare(String(left.id || '')))
            .slice(0, 6);
        const recentContainer = document.getElementById('recentPlantsList');
        recentContainer.innerHTML = recent.map(p => `
            <div class="d-flex justify-content-between align-items-center border rounded px-2 py-1 bg-white">
                <div>
                    <div class="fw-semibold">${escapeHtml(p.name)}</div>
                    <small class="text-muted">${escapeHtml(p.species?.name || 'Espèce inconnue')}</small>
                </div>
                <span class="badge ${badgeClassForState(p.plantState)}">${p.plantState || 'N/A'}</span>
            </div>
        `).join('') || '<small class="text-muted">Aucune plante</small>';

        await loadForestsForSelect(forests);
        fillPlantSelectors(plants);
        fillPredictPlantSelector();
        await loadLiveEffectsImpact();
        await loadGreenhouseAlerts();
        await loadRoiPanels();
    } catch (error) {
        console.error('Erreur dashboard:', error);
        showDashboardFeedback(`Erreur dashboard: ${error.message}`, 'danger');
    } finally {
        if (refreshButton) {
            refreshButton.disabled = false;
            refreshButton.innerHTML = '<i class="fas fa-rotate"></i> Rafraîchir';
        }
    }
}

function fillPlantSelectors(plants) {
    const left = document.getElementById('compareLeftPlant');
    const right = document.getElementById('compareRightPlant');
    if (!left || !right) return;

    const previousLeft = left.value;
    const previousRight = right.value;

    const options = ['<option value="" selected disabled>Choisir une plante...</option>']
        .concat(
            [...plants]
                .sort((a, b) => String(a.name || '').localeCompare(String(b.name || '')))
                .map(plant => `<option value="${plant.id}">${escapeHtml(plant.name)} · ${escapeHtml(plant.species?.name || 'N/A')}</option>`)
        )
        .join('');

    left.innerHTML = options;
    right.innerHTML = options;

    if (previousLeft && [...left.options].some(option => option.value === previousLeft)) {
        left.value = previousLeft;
    }
    if (previousRight && [...right.options].some(option => option.value === previousRight)) {
        right.value = previousRight;
    }
}

async function loadLiveEffectsImpact() {
    const table = document.getElementById('liveEffectsTable');
    if (!table) return;

    const impacts = await getJson(`${URLS.GREENHOUSE}/live-effects?limit=12`);
    if (!impacts.length) {
        table.innerHTML = '<tr><td colspan="4" class="text-muted">Aucun effet actif.</td></tr>';
        return;
    }

    table.innerHTML = impacts.map(item => `
        <tr>
            <td>${escapeHtml(item.forestName || 'N/A')}</td>
            <td class="fw-semibold">${escapeHtml(item.plantName || 'N/A')}</td>
            <td>${Number(item.activeEffectsCount || 0)} actif(s)</td>
            <td>${Number(item.stressPct || 0).toFixed(1)}%</td>
        </tr>
    `).join('');
}

async function loadGreenhouseAlerts() {
    const table = document.getElementById('greenhouseAlertsTable');
    if (!table) return;

    const alerts = await getJson(`${URLS.GREENHOUSE}/alerts?hours=24&limit=10`);
    if (!alerts.length) {
        table.innerHTML = '<tr><td colspan="4" class="text-muted">Aucune alerte active.</td></tr>';
        return;
    }

    table.innerHTML = alerts.map(alert => {
        const sev = String(alert.severity || 'UNKNOWN').toUpperCase();
        const badge = sev === 'CRITICAL' ? 'bg-danger' : 'bg-warning text-dark';
        return `
            <tr>
                <td><span class="badge ${badge}">${escapeHtml(sev)}</span></td>
                <td>${escapeHtml(alert.plantName || 'N/A')}</td>
                <td>${escapeHtml(alert.type || 'N/A')}</td>
                <td class="small">${escapeHtml(alert.message || '')}</td>
            </tr>
        `;
    }).join('');
}

async function runPlantComparison() {
    const leftId = document.getElementById('compareLeftPlant')?.value;
    const rightId = document.getElementById('compareRightPlant')?.value;
    const result = document.getElementById('compareResult');
    if (!leftId || !rightId || !result) {
        showDashboardFeedback('Sélectionnez deux plantes à comparer.', 'warning');
        return;
    }

    if (leftId === rightId) {
        showDashboardFeedback('Choisissez deux plantes différentes pour comparer.', 'warning');
        return;
    }

    try {
        const data = await getJson(`${URLS.PLANTS}/compare?leftId=${encodeURIComponent(leftId)}&rightId=${encodeURIComponent(rightId)}`);
        const cmp = data?.comparison || {};
        const sameSpecies = cmp.sameSpecies ? 'Oui' : 'Non';
        const stateChanged = cmp.stateChanged ? 'Oui' : 'Non';
        const fmt = val => Number(val || 0).toFixed(2);

        result.innerHTML = `
            <div class="border rounded p-2 bg-white">
                <div><strong>Même espèce:</strong> ${sameSpecies}</div>
                <div><strong>Changement d'état:</strong> ${stateChanged}</div>
                <div><strong>Δ Stress:</strong> ${fmt(cmp.stressIndexDelta)}</div>
                <div><strong>Δ Hauteur (cm):</strong> ${fmt(cmp.heightCmDelta)}</div>
                <div><strong>Δ Température:</strong> ${fmt(cmp.sensorDelta?.temperature)}</div>
                <div><strong>Δ Humidité:</strong> ${fmt(cmp.sensorDelta?.humidity)}</div>
                <div><strong>Δ Lux:</strong> ${fmt(cmp.sensorDelta?.lux)}</div>
            </div>
        `;
        showDashboardFeedback('Comparaison mise à jour.', 'success');
    } catch (error) {
        showDashboardFeedback(`Comparaison impossible: ${error.message}`, 'danger');
    }
}

async function emitSensorReading() {
    const forestId = document.getElementById('sensorForestSelect')?.value;
    const profile = document.getElementById('sensorProfileSelect')?.value || 'NORMAL';
    const liveLog = document.getElementById('sensorLiveLog');
    const counter = document.getElementById('sensorStreamCount');
    if (!forestId) {
        stopSensorStream();
        showDashboardFeedback('Sélectionnez une forêt cible pour le stream capteur.', 'warning');
        return;
    }

    try {
        const tick = await getJson(`${URLS.GREENHOUSE}/sensor-stream/tick`, {
            method: 'POST',
            body: JSON.stringify({ forestId, profile })
        });

        emittedSensorCount += Number(tick.createdReadings || 0);
        if (counter) counter.textContent = String(emittedSensorCount);
        if (liveLog) {
            liveLog.innerHTML = `
                <div class="border rounded p-2 bg-white">
                    <div><strong>Dernière émission</strong> · ${new Date().toLocaleTimeString()}</div>
                    <div>Forêt: ${escapeHtml(tick.forestName || forestId)} · Profil: ${escapeHtml(profile)}</div>
                    <div>Lectures créées: ${tick.createdReadings} · Échecs: ${tick.failedReadings}</div>
                    <div>Moyennes · Temp: ${tick.avgTemperature}°C · Humidité: ${tick.avgHumidity}% · Lux: ${tick.avgLux} · Pluie: ${tick.avgRainfall} mm</div>
                </div>
            `;
        }
        await loadDashboardData();
        // inclure le chargement des alertes météo
        await loadWeatherAlerts();
    } catch (error) {
        stopSensorStream();
        showDashboardFeedback(`Stream capteur arrêté: ${error.message}`, 'danger');
    }
}

function startSensorStream() {
    if (sensorStreamInterval) return;
    const startBtn = document.getElementById('startSensorStreamBtn');
    const stopBtn = document.getElementById('stopSensorStreamBtn');
    sensorStreamInterval = setInterval(emitSensorReading, 5000);
    emitSensorReading();
    if (startBtn) startBtn.disabled = true;
    if (stopBtn) stopBtn.disabled = false;
    showDashboardFeedback('Simulation capteurs forêt démarrée (tick toutes les 5 secondes).', 'info');
}

function stopSensorStream() {
    if (!sensorStreamInterval) return;
    clearInterval(sensorStreamInterval);
    sensorStreamInterval = null;
    const startBtn = document.getElementById('startSensorStreamBtn');
    const stopBtn = document.getElementById('stopSensorStreamBtn');
    if (startBtn) startBtn.disabled = false;
    if (stopBtn) stopBtn.disabled = true;
    showDashboardFeedback('Simulation capteurs arrêtée.', 'secondary');
}

async function loadForestsForSelect(existingForests = null) {
    const forests = existingForests || await getJson(URLS.FORESTS);
    const select = document.getElementById('forestSelect');
    const sensorSelect = document.getElementById('sensorForestSelect');
    if (!select || !sensorSelect) return;

    const previousMain = select.value;
    const previousSensor = sensorSelect.value;

    const options = [...forests]
        .sort((left, right) => String(left.name || '').localeCompare(String(right.name || '')))
        .map(f => `<option value="${f.id}">${escapeHtml(f.name)}</option>`)
        .join('');

    select.innerHTML = `<option value="" selected disabled>Choisir une forêt...</option>${options}`;
    sensorSelect.innerHTML = `<option value="" selected disabled>Forêt cible du stream...</option>${options}`;

    if (previousMain && [...select.options].some(option => option.value === previousMain)) {
        select.value = previousMain;
    }
    if (previousSensor && [...sensorSelect.options].some(option => option.value === previousSensor)) {
        sensorSelect.value = previousSensor;
    }
}

async function sendStimulus(type) {
    const forestId = document.getElementById('forestSelect').value;
    if (!forestId) {
        showDashboardFeedback('Choisissez d’abord une forêt.', 'warning');
        return;
    }

    const payload = {
        type,
        forestId,
        intensity: type === 'HEATWAVE' ? 8.0 : 20.0,
        durationHours: 12
    };

    try {
        await getJson(URLS.STIMULI, {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        showDashboardFeedback(`Stimulus ${type} appliqué.`, 'success');
        await loadDashboardData();
    } catch (error) {
        console.error('Erreur stimulus:', error);
        showDashboardFeedback(`Erreur stimulus: ${error.message}`, 'danger');
    }
}

// ==================== ALERTES MÉTÉO ====================

async function loadWeatherForestFilter() {
    const select = document.getElementById('weatherForestFilter');
    if (!select) return;
    try {
        const forests = await fetch('/api/forests').then(r => r.json());
        forests.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.id;
            opt.textContent = f.name;
            select.appendChild(opt);
        });
    } catch (e) {
        console.error('Erreur chargement forêts (filtre alertes):', e);
    }
}

async function loadWeatherAlerts() {
    const tbody = document.getElementById('weatherAlertsTable');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="6" class="text-muted text-center">Chargement des alertes...</td></tr>';

    const forestId   = document.getElementById('weatherForestFilter')?.value || '';
    const activeOnly = document.getElementById('activeOnlyCheck')?.checked || false;

    let url = '/api/weather/alerts?';
    if (forestId)   url += `forestId=${encodeURIComponent(forestId)}&`;
    if (activeOnly) url += `activeOnly=true&`;

    try {
        const alerts = await fetch(url).then(r => r.json());

        if (!alerts.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-muted text-center">Aucune alerte météo</td></tr>';
            return;
        }

        const typeIcon = t => t === 'heatwave' ? '🔥' : t === 'frost' ? '❄️' :
                              t === 'heavy_rain' ? '🌧️' : t === 'high_wind' ? '💨' : '⚠️';

        tbody.innerHTML = alerts.map(alert => {
            const severityClass = alert.severity === 'high'   ? 'bg-danger' :
                                  alert.severity === 'medium' ? 'bg-warning text-dark' : 'bg-info';
            const severityText  = alert.severity === 'high'   ? 'CRITIQUE' :
                                  alert.severity === 'medium' ? 'ÉLEVÉE' : 'INFO';

            const statusBadge = alert.acknowledged
                ? `<span class="badge bg-success">Acquitté</span>`
                : `<span class="badge bg-secondary">En attente</span>`;

            const actionBtn = !alert.acknowledged
                ? `<button class="btn btn-sm btn-outline-success" onclick="acknowledgeWeatherAlert('${alert.id}')">
                       <i class="fas fa-check"></i> Acquitter
                   </button>`
                : `<span class="text-muted small">${alert.acknowledgedAt ? new Date(alert.acknowledgedAt).toLocaleString() : '—'}</span>`;

            return `
                <tr>
                    <td><small>${alert.timestamp ? new Date(alert.timestamp).toLocaleString() : '—'}</small></td>
                    <td><span class="badge bg-light text-dark">${typeIcon(alert.type)} ${alert.type}</span></td>
                    <td><span class="badge ${severityClass}">${severityText}</span></td>
                    <td><small>${alert.coords ? `${alert.coords[0].toFixed(4)}, ${alert.coords[1].toFixed(4)}` : 'N/A'}</small></td>
                    <td>${statusBadge}</td>
                    <td>${actionBtn}</td>
                </tr>`;
        }).join('');

    } catch (error) {
        console.error('Erreur chargement alertes météo:', error);
        tbody.innerHTML = '<tr><td colspan="6" class="text-danger text-center">Erreur de chargement</td></tr>';
    }
}

async function acknowledgeWeatherAlert(alertId) {
    try {
        const response = await fetch(`/api/weather/alerts/${alertId}/ack`, { method: 'POST' });
        if (response.ok) await loadWeatherAlerts();
    } catch (error) {
        console.error('Erreur acquittement:', error);
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await loadDashboardData();
    await loadWeatherForestFilter();
});

// ==================== PRÉDICTIONS ====================

let predictionChart = null;

async function loadPredictions() {
    const plantId = document.getElementById('predictPlantSelect')?.value;
    const days = document.getElementById('predictDaysSelect')?.value || 7;
    
    if (!plantId) {
        showDashboardFeedback('Veuillez sélectionner une plante', 'warning');
        return;
    }
    
    const alertDiv = document.getElementById('predictionAlert');
    const alertMessage = document.getElementById('predictionAlertMessage');
    
    try {
        const response = await fetch(`/api/predictions/plant/${plantId}?days=${days}`);
        const data = await response.json();
        
        if (data.error) {
            showDashboardFeedback(data.error, 'danger');
            return;
        }
        
        if (data.alert) {
            alertMessage.textContent = data.alert.message;
            alertDiv.classList.remove('d-none');
        } else {
            alertDiv.classList.add('d-none');
        }
        
        const labels = data.stressPredictions.map(p => {
            const date = new Date(p.date);
            return date.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit' });
        });
        
        const stressValues = data.stressPredictions.map(p => (p.value * 100).toFixed(1));
        const heightValues = data.heightPredictions.map(p => p.value.toFixed(1));
        
        labels.unshift('Aujourd\'hui');
        stressValues.unshift((data.currentStress * 100).toFixed(1));
        heightValues.unshift(data.currentHeight.toFixed(1));
        
        if (predictionChart) {
            predictionChart.destroy();
        }
        
        const ctx = document.getElementById('predictionChart').getContext('2d');
        predictionChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Stress (%)',
                        data: stressValues,
                        borderColor: 'rgb(255, 99, 132)',
                        backgroundColor: 'rgba(255, 99, 132, 0.1)',
                        borderWidth: 2,
                        borderDash: [5, 5],
                        tension: 0.3,
                        fill: true,
                        yAxisID: 'y'
                    },
                    {
                        label: 'Hauteur (cm)',
                        data: heightValues,
                        borderColor: 'rgb(75, 192, 192)',
                        backgroundColor: 'rgba(75, 192, 192, 0.1)',
                        borderWidth: 2,
                        borderDash: [],
                        tension: 0.3,
                        fill: true,
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                interaction: {
                    mode: 'index',
                    intersect: false,
                },
                plugins: {
                    title: {
                        display: true,
                        text: `Prédictions pour ${data.plantName}`
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                let label = context.dataset.label || '';
                                let value = context.raw;
                                let unit = context.dataset.label.includes('Stress') ? '%' : ' cm';
                                return `${label}: ${value}${unit}`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        title: {
                            display: true,
                            text: 'Stress (%)'
                        },
                        min: 0,
                        max: 100,
                        ticks: {
                            callback: function(value) {
                                return value + '%';
                            }
                        }
                    },
                    y1: {
                        position: 'right',
                        title: {
                            display: true,
                            text: 'Hauteur (cm)'
                        },
                        min: 0,
                        grid: {
                            drawOnChartArea: false,
                        },
                    }
                }
            }
        });
        
        showDashboardFeedback('Prédictions chargées', 'success');
    } catch (error) {
        console.error('Erreur chargement prédictions:', error);
        showDashboardFeedback(`Erreur: ${error.message}`, 'danger');
    }
}

function fillPredictPlantSelector() {
    const select = document.getElementById('predictPlantSelect');
    if (!select || !cachedPlants) return;
    
    const options = ['<option value="" selected disabled>-- Choisir une plante --</option>']
        .concat(
            [...cachedPlants]
                .sort((a, b) => String(a.name || '').localeCompare(String(b.name || '')))
                .map(plant => `<option value="${plant.id}">${escapeHtml(plant.name)} · ${escapeHtml(plant.species?.name || 'N/A')}</option>`)
        )
        .join('');
    
    select.innerHTML = options;
} 
