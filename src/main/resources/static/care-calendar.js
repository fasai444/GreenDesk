let tasks = [];
let plants = [];

const container = document.getElementById('tasksContainer');

const loading = document.getElementById('loading');

const emptyState = document.getElementById('emptyState');

const statusMessage = document.getElementById('statusMessage');

const priorityFilter = document.getElementById('priorityFilter');

const statusFilter = document.getElementById('statusFilter');

const searchInput = document.getElementById('searchInput');

// =====================================================
// API
// =====================================================

async function loadTasks() {

    try {

        const response = await fetch(
            '/api/care-tasks',
            {
                credentials: 'include'
            }
        );

        const tasks =
            await response.json();

        // FILTER VALUES

        const priority =
            document.getElementById('priorityFilter').value;

        const status =
            document.getElementById('statusFilter').value;

        const search =
            document.getElementById('searchInput')
                .value
                .toLowerCase();

        // FILTERING

        const filteredTasks =
            tasks.filter(task => {

                const matchPriority =
                    !priority
                    || task.priority === priority;

                const matchStatus =
                    !status
                    || task.status === status;

                const matchSearch =
                    !search
                    || (
                        task.plantName &&
                        task.plantName
                            .toLowerCase()
                            .includes(search)
                    );

                return (
                    matchPriority
                    && matchStatus
                    && matchSearch
                );

            });

        // RENDER

        renderTasks(filteredTasks);

        // STATS BASED ON FILTERED TASKS

        updateStats(filteredTasks);

    } catch (e) {

        console.error(e);

    }
}

function updateStats(tasks) {

    const total =
        tasks.length;

    const pending =
        tasks.filter(
            t => t.status === 'PENDING'
        ).length;

    const done =
        tasks.filter(
            t => t.status === 'DONE'
        ).length;

    const canceled =
        tasks.filter(
            t => t.status === 'CANCELED'
        ).length;

    document.getElementById('totalTasks')
        .textContent = total;

    document.getElementById('pendingTasks')
        .textContent = pending;

    document.getElementById('doneTasks')
        .textContent = done;

    document.getElementById('canceledTasks')
        .textContent = canceled;
}

async function loadPlants() {

    try {

        const response = await fetch('/api/plants', {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Impossible de charger les plantes');
        }

        plants = await response.json();

        const select = document.getElementById('manualPlantSelect');

        select.innerHTML = `
            <option value="">
                Sélectionner une plante
            </option>
        `;

        plants.forEach(plant => {

            select.innerHTML += `
                <option value="${plant.id}">
                    ${plant.name}
                </option>
            `;
        });

    } catch (error) {

        console.error(error);
    }
}

async function markDone(taskId) {

    try {

        await fetch(`/api/care-tasks/${taskId}/done`, {
            method: 'POST',
            credentials: 'include'
        });

        await loadTasks();

    }

    catch (error) {

        console.error(error);
    }
}

async function cancelTask(taskId) {

    try {

        await fetch(`/api/care-tasks/${taskId}`, {
            method: 'DELETE'
        });

        await loadTasks();

    }

    catch (error) {

        console.error(error);
    }
}

async function rescheduleTask(taskId) {

    alert('Fonction reschedule bientôt connectée.');
}

// =====================================================
// UI
// =====================================================

function showLoading(show) {

    if (show) {
        loading.classList.remove('d-none');
    }

    else {
        loading.classList.add('d-none');
    }
}

function priorityBadge(priority) {

    switch(priority) {

        case 'LOW':
            return 'bg-success-subtle text-success';

        case 'MEDIUM':
            return 'bg-warning-subtle text-warning';

        case 'HIGH':
            return 'bg-orange-100 text-warning';

        case 'CRITICAL':
            return 'bg-danger-subtle text-danger';

        default:
            return 'bg-secondary-subtle text-secondary';
    }
}

function statusBadge(status) {

    switch(status) {

        case 'PENDING':
            return 'bg-primary-subtle text-primary';

        case 'DONE':
            return 'bg-success-subtle text-success';

        case 'CANCELED':
            return 'bg-danger-subtle text-danger';

        default:
            return 'bg-secondary-subtle text-secondary';
    }
}

function filterTasks() {

    let filtered = [...tasks];

    const priority = priorityFilter.value;

    const status = statusFilter.value;

    const search = searchInput.value.toLowerCase();

    if (priority) {

        filtered = filtered.filter(
            task => task.priority === priority
        );
    }

    if (status) {

        filtered = filtered.filter(
            task => task.status === status
        );
    }

    if (search) {

        filtered = filtered.filter(task =>
            (task.plantName || '')
                .toLowerCase()
                .includes(search)
        );
    }

    return filtered;
}

function renderTasks() {

    const filteredTasks = filterTasks();

    container.innerHTML = '';

    if (filteredTasks.length === 0) {

        emptyState.classList.remove('d-none');

        return;
    }

    emptyState.classList.add('d-none');

    filteredTasks.forEach(task => {

        const card = document.createElement('div');

        card.className = `
            glass
            rounded-5
            p-4
            shadow-sm
            fade-in
        `;

        card.innerHTML = `

            <div class="d-flex flex-column flex-lg-row justify-content-between gap-4 mb-4">

                <div>

                    <h2 class="fw-bold mb-2">
                        ${task.type}
                    </h2>

                    <p class="text-muted mb-0">
                        ${task.description || 'Aucune description'}
                    </p>

                </div>

                <div class="d-flex gap-2 flex-wrap align-items-start">

                    <span class="badge px-3 py-2 fs-6 rounded-pill ${priorityBadge(task.priority)}">
                        ${task.priority}
                    </span>

                    <span class="badge px-3 py-2 fs-6 rounded-pill ${statusBadge(task.status)}">
                        ${task.status}
                    </span>

                </div>

            </div>

            <div class="row g-3 mb-4">

                <div class="col-lg-3">

                    <div class="glass-soft rounded-4 p-3 h-100">

                        <small class="text-muted d-block mb-1">
                            Plante
                        </small>

                        <div class="fw-semibold fs-5">
                            ${task.plantName || task.plantId}
                        </div>

                    </div>

                </div>

                <div class="col-lg-3">

                    <div class="glass-soft rounded-4 p-3 h-100">

                        <small class="text-muted d-block mb-1">
                            Score WNS
                        </small>

                        <div class="fw-semibold fs-5">
                            ${Math.round((task.wnsScore || 0) * 100)}%
                        </div>

                    </div>

                </div>

                <div class="col-lg-3">

                    <div class="glass-soft rounded-4 p-3 h-100">

                        <small class="text-muted d-block mb-1">
                            Échéance
                        </small>

                        <div class="fw-semibold">
                            ${
                                task.dueAt
                                    ? new Date(task.dueAt).toLocaleString()
                                    : 'N/A'
                            }
                        </div>

                    </div>

                </div>

                <div class="col-lg-3">

                    <div class="glass-soft rounded-4 p-3 h-100">

                        <small class="text-muted d-block mb-1">
                            Google Calendar
                        </small>

                        <div class="fw-semibold small">
                            ${task.externalId || 'Non synchronisé'}
                        </div>

                    </div>

                </div>

            </div>

            <div class="d-flex flex-wrap gap-3">

                ${
                    task.status === 'PENDING'
                    ? `
                        <button
                            onclick="markDone('${task.id}')"
                            class="btn btn-success"
                        >
                            <i class="fas fa-check me-2"></i>
                            Done
                        </button>

                        <button
                            onclick="cancelTask('${task.id}')"
                            class="btn btn-outline-danger"
                        >
                            <i class="fas fa-xmark me-2"></i>
                            Cancel
                        </button>

                        <button
                            onclick="rescheduleTask('${task.id}')"
                            class="btn btn-outline-secondary"
                        >
                            <i class="fas fa-clock me-2"></i>
                            Reschedule
                        </button>
                    `
                    : `
                        <div class="text-muted fw-semibold">
                            Tâche clôturée
                        </div>
                    `
                }

            </div>
        `;

        container.appendChild(card);
    });
}

async function createManualTask() {

    try {

        const plantId =
            document.getElementById('manualPlantSelect').value;

        if (!plantId) {

            alert('Sélectionne une plante');

            return;
        }

        const payload = {

            plantId: plantId,

            type:
                document.getElementById('manualType').value,

            priority:
                document.getElementById('manualPriority').value,

            description:
                'Tâche créée manuellement',

            dueAt:
                new Date(
                    Date.now() + 86400000
                ).toISOString()
        };

        const response = await fetch(
            '/api/care-tasks/manual',
            {
                method: 'POST',

                headers: {
                    'Content-Type': 'application/json'
                },

                body: JSON.stringify(payload),
                credentials: 'include'
            }
        );

        if (!response.ok) {

            throw new Error(
                'Erreur création tâche'
            );
        }

        await loadTasks();

    } catch (error) {

        console.error(error);

        alert(
            'Impossible de créer la tâche'
        );
    }
}

// =====================================================
// EVENTS
// =====================================================

priorityFilter.addEventListener('change', renderTasks);

statusFilter.addEventListener('change', renderTasks);

searchInput.addEventListener('input', renderTasks);

// =====================================================
// INIT
// =====================================================

document.addEventListener('DOMContentLoaded', async () => {

    if (window.AUTH) {
        await AUTH.requireAuth();
    }

    loadPlants();
    loadTasks();

    setInterval(loadTasks, 30000);
});