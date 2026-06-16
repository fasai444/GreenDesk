function showDiseaseFeedback(message, variant = 'danger') {
    const feedback = document.getElementById('diseaseFeedback');
    if (!feedback) return;
    feedback.className = `alert alert-${variant} app-feedback mb-3`;
    feedback.textContent = message;
    feedback.classList.remove('d-none');
}

function clearDiseaseFeedback() {
    const feedback = document.getElementById('diseaseFeedback');
    if (!feedback) return;
    feedback.classList.add('d-none');
    feedback.textContent = '';
}

async function postCommand(url) {
    const response = await fetch(url, { method: 'POST' });
    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || `HTTP ${response.status}`);
    }
}

function setActionLoading(buttonId, loading, idleHtml, loadingHtml) {
    const button = document.getElementById(buttonId);
    if (!button) return;
    button.disabled = loading;
    button.innerHTML = loading ? loadingHtml : idleHtml;
}

function updateGridCounters({ healthy, diseased, empty }) {
    const healthyNode = document.getElementById('healthyCount');
    const diseasedNode = document.getElementById('diseasedCount');
    const emptyNode = document.getElementById('emptyCount');
    if (healthyNode) healthyNode.textContent = String(healthy);
    if (diseasedNode) diseasedNode.textContent = String(diseased);
    if (emptyNode) emptyNode.textContent = String(empty);
}

async function tick() {
    clearDiseaseFeedback();
    setActionLoading('tickBtn', true, '<i class="fas fa-forward-step me-1"></i> Tick', '<span class="spinner-border spinner-border-sm me-1"></span>Tick...');
    try {
        await postCommand('/api/ecosystem/tick');
        await loadGrid();
        showDiseaseFeedback('Tick exécuté.', 'success');
    } catch (error) {
        showDiseaseFeedback(`Tick impossible: ${error.message}`);
    } finally {
        setActionLoading('tickBtn', false, '<i class="fas fa-forward-step me-1"></i> Tick', '');
    }
}

async function simulate() {
    clearDiseaseFeedback();
    const n = Number.parseInt(document.getElementById('nbTicks')?.value || '0', 10);
    if (!Number.isFinite(n) || n < 1) {
        showDiseaseFeedback('Le nombre de ticks doit être supérieur ou égal à 1.', 'warning');
        return;
    }

    setActionLoading('simulateBtn', true, '<i class="fas fa-forward-fast me-1"></i> Simuler', '<span class="spinner-border spinner-border-sm me-1"></span>Simulation...');
    try {
        await postCommand(`/api/ecosystem/simulate/${n}`);
        await loadGrid();
        showDiseaseFeedback(`Simulation terminée (${n} ticks).`, 'success');
    } catch (error) {
        showDiseaseFeedback(`Simulation impossible: ${error.message}`);
    } finally {
        setActionLoading('simulateBtn', false, '<i class="fas fa-forward-fast me-1"></i> Simuler', '');
    }
}

async function loadGrid() {
    clearDiseaseFeedback();
    setActionLoading('refreshDiseaseBtn', true, '<i class="fas fa-rotate me-1"></i> Rafraîchir', '<span class="spinner-border spinner-border-sm me-1"></span>Chargement...');

    try {
        const res = await fetch('/api/ecosystem/cells');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const cells = await res.json();
        const grid = document.getElementById('grid');
        grid.innerHTML = '';

        let maxX = 0;
        let maxY = 0;
        cells.forEach(line => {
            const match = line.match(/Cell \[(\d+),(\d+)\]/);
            if (!match) return;
            maxX = Math.max(maxX, Number.parseInt(match[1], 10));
            maxY = Math.max(maxY, Number.parseInt(match[2], 10));
        });

        grid.style.gridTemplateColumns = `repeat(${maxX + 1}, 42px)`;

        const map = {};
        cells.forEach(line => {
            const match = line.match(/Cell \[(\d+),(\d+)\] Plant: (.+) \| Disease: (.+) \| Severity: (.+)/);
            if (!match) return;
            map[`${match[1]},${match[2]}`] = {
                x: match[1],
                y: match[2],
                plantId: match[3],
                disease: match[4],
                severity: match[5]
            };
        });

        const counters = { healthy: 0, diseased: 0, empty: 0 };

        for (let y = 0; y <= maxY; y += 1) {
            for (let x = 0; x <= maxX; x += 1) {
                const key = `${x},${y}`;
                const cell = document.createElement('div');
                cell.classList.add('cell');

                const data = map[key];
                if (!data || data.plantId === 'null') {
                    cell.classList.add('empty');
                    counters.empty += 1;
                } else if (data.disease !== 'Healthy') {
                    cell.classList.add('diseased', 'pulse');
                    counters.diseased += 1;
                } else {
                    cell.classList.add('healthy');
                    counters.healthy += 1;
                }

                cell.onclick = () => showDetails(data);
                grid.appendChild(cell);
            }
        }

        updateGridCounters(counters);
    } catch (error) {
        showDiseaseFeedback(`Chargement impossible: ${error.message}`);
    } finally {
        setActionLoading('refreshDiseaseBtn', false, '<i class="fas fa-rotate me-1"></i> Rafraîchir', '');
    }
}

function showDetails(cell) {
    const panel = document.getElementById('cellDetails');
    if (!panel) return;

    if (!cell) {
        panel.textContent = 'Cellule vide';
        return;
    }

    panel.textContent =
`Position : (${cell.x}, ${cell.y})
Plante ID : ${cell.plantId}
État : ${cell.disease}
Sévérité : ${cell.severity}`;
}

document.addEventListener('DOMContentLoaded', async () => {
    if (!await AUTH.requireAuth()) return;
    loadGrid();
});