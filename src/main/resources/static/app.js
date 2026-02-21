// CONFIGURATION DES ENDPOINTS API
const API = {
    FORESTS: '/api/forests',
    PLANTS: '/plants',
    SPECIES: '/api/species',
    SEASONS: '/api/seasons',
    EFFECTS: '/api/effects'
};

// Variables globales
let currentX, currentY;
let plantModal, actionModal;
let autoPlayInterval = null;
let selectedPlantId = null;

// ────────────────────────────────────────────────
// Initialisation
// ────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
    await loadForests();
    await loadSpeciesForModal();
    await checkAndCreateDemoData();

    // Événements des boutons
    document.getElementById('btnCreateForest')?.addEventListener('click', createForest);
    document.getElementById('btnDeleteForest')?.addEventListener('click', deleteForest);
    document.getElementById('btnPlantConfirm')?.addEventListener('click', confirmPlanting);
    document.getElementById('btnAdvanceSeason')?.addEventListener('click', advanceSeason);
    document.getElementById('btnAutoPlay')?.addEventListener('click', toggleAutoPlay);
    document.getElementById('btnApplyEffect')?.addEventListener('click', applyEffectToPlant);
    document.getElementById('btnRemovePlant')?.addEventListener('click', deletePlantFromForest);
});

// ────────────────────────────────────────────────
// Données de démo
// ────────────────────────────────────────────────
async function checkAndCreateDemoData() {
    try {
        const res = await fetch(API.FORESTS);
        const forests = await res.json();

        if (forests.length === 0) {
            console.log("Aucune forêt → création démo...");

            // 1. Espèce démo
            const speciesRes = await fetch(API.SPECIES, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: "Tomate Cerise (Démo)",
                    optimalWaterNeeds: 280,
                    optimalTemperature: 24,
                    optimalLuxNeeds: 1800,
                    baseGrowthRate: 1.6
                })
            });
            const demoSpecies = await speciesRes.json();

            // 2. Forêt démo
            const forestRes = await fetch(API.FORESTS, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: "Jardin Potager Démo", width: 8, height: 6 })
            });
            const demoForest = await forestRes.json();

            // 3. Une plante démo
            const plantRes = await fetch(`${API.PLANTS}/create?name=Tomate_Maman&speciesId=${demoSpecies.id}`, {
                method: 'POST'
            });
            const demoPlant = await plantRes.json();

            // 4. Placement
            await fetch(`${API.FORESTS}/${demoForest.id}/plants/${demoPlant.id}?x=4&y=3`, { method: 'POST' });

            alert("🌱 Forêt de démonstration créée !");
            await loadForests(demoForest.id);
        }
    } catch (err) {
        console.error("Échec création démo", err);
    }
}

// ────────────────────────────────────────────────
// Gestion des forêts
// ────────────────────────────────────────────────
async function loadForests(selectId = null) {
    try {
        const res = await fetch(API.FORESTS);
        const forests = await res.json();
        const select = document.getElementById('forestSelect');

        select.innerHTML = '<option value="" disabled selected>-- Choisir une forêt --</option>';

        forests.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.id;
            opt.textContent = `${f.name} (${f.width} × ${f.height})`;
            select.appendChild(opt);
        });

        if (selectId && forests.some(f => f.id === selectId)) {
            select.value = selectId;
            loadForestDetails();
        } else if (select.value) {
            loadForestDetails();
        }
    } catch (err) {
        console.error("Erreur loadForests", err);
    }
}

async function createForest() {
    const name = document.getElementById('newForestName')?.value.trim();
    const w = parseInt(document.getElementById('newForestW')?.value);
    const h = parseInt(document.getElementById('newForestH')?.value);

    if (!name || isNaN(w) || isNaN(h) || w < 3 || h < 3) {
        return alert("Nom obligatoire + dimensions ≥ 3");
    }

    try {
        const res = await fetch(API.FORESTS, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, width: w, height: h })
        });

        if (!res.ok) throw new Error(await res.text());

        const newForest = await res.json();
        bootstrap.Modal.getInstance(document.getElementById('createForestModal'))?.hide();

        document.getElementById('newForestName').value = '';
        await loadForests(newForest.id);
        alert(`Forêt « ${newForest.name} » créée !`);
    } catch (err) {
        alert("Erreur création forêt : " + err.message);
    }
}

async function deleteForest() {
    const id = document.getElementById('forestSelect')?.value;
    if (!id || !confirm("Supprimer la forêt et toutes ses plantes ?")) return;

    try {
        await fetch(`${API.FORESTS}/${id}`, { method: 'DELETE' });
        document.getElementById('gridContainer').innerHTML = '';
        document.getElementById('seasonPanel')?.classList.add('d-none');
        await loadForests();
    } catch (err) {
        alert("Erreur suppression");
    }
}

async function resetCurrentForestPlants() {
    const forestId = document.getElementById('forestSelect')?.value;
    if (!forestId) {
        alert("Sélectionne une forêt d'abord !");
        return;
    }

    if (!confirm("Vraiment supprimer TOUTES les plantes de cette forêt ?\nCeci est irréversible.")) {
        return;
    }

    try {
        // Essaie de supprimer toutes les plantes associées (endpoint à créer côté backend si absent)
        // Si ton backend n'a pas cet endpoint, on supprime la forêt et on la recrée après
        const res = await fetch(`${API.FORESTS}/${forestId}/plants`, { method: 'DELETE' });

        if (res.ok || res.status === 404) {  // 404 = peut-être pas implémenté
            alert("Forêt réinitialisée (toutes plantes supprimées) ✓");
            await loadForestDetails();
        } else {
            // Alternative : supprimer et recréer la forêt (plus lourd mais fonctionne)
            const forestData = await (await fetch(`${API.FORESTS}/${forestId}`)).json();
            await fetch(`${API.FORESTS}/${forestId}`, { method: 'DELETE' });
            
            const newForestRes = await fetch(API.FORESTS, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: forestData.name + " (reset)",
                    width: forestData.width,
                    height: forestData.height
                })
            });
            const newForest = await newForestRes.json();
            await loadForests(newForest.id);
            alert("Forêt réinitialisée en en recréant une nouvelle.");
        }
    } catch (err) {
        console.error(err);
        alert("Erreur lors de la réinitialisation : " + err.message);
    }
}

// ────────────────────────────────────────────────
// Affichage grille & détails
// ────────────────────────────────────────────────
async function loadForestDetails() {
    const forestId = document.getElementById('forestSelect')?.value;
    if (!forestId) return;

    try {
        // Forêt + dimensions
        const forestsRes = await fetch(API.FORESTS);
        const forests = await forestsRes.json();
        const forest = forests.find(f => f.id === forestId);
        if (!forest) return;

        // Plantes
        const plantsRes = await fetch(`${API.FORESTS}/${forestId}/plants`);
        const plants = await plantsRes.json();

        // Saison
        await loadSeason(forestId);

        document.getElementById('forestTitle').textContent = `${forest.name} — ${plants.length} plante(s)`;
        document.getElementById('gridLegend')?.classList.remove('d-none');

        drawGrid(forest, plants);
    } catch (err) {
        console.error("Erreur loadForestDetails", err);
    }
}

function drawGrid(forest, plants) {
    const container = document.getElementById('gridContainer');
    if (!container) return;

    container.innerHTML = '';

    const grid = document.createElement('div');
    grid.className = 'forest-grid';
    grid.style.gridTemplateColumns = `repeat(${forest.width}, 54px)`;

    for (let y = 0; y < forest.height; y++) {
        for (let x = 0; x < forest.width; x++) {
            const cell = document.createElement('div');
            cell.className = 'forest-cell';

            const plant = plants.find(p => p.position?.x === x && p.position?.y === y);

            if (plant) {
                cell.classList.add('occupied', `status-${plant.state?.toLowerCase() || 'unknown'}`);
                cell.innerHTML = '<i class="fas fa-seedling"></i>';
                cell.title = `${plant.name}\n${plant.species?.name || '?'}\n${plant.state || 'Inconnu'}`;
                cell.onclick = () => openPlantActionModal(plant);
            } else {
                cell.title = `Vide (${x},${y})`;
                cell.onclick = () => openPlantModal(x, y);
            }

            grid.appendChild(cell);
        }
    }

    container.appendChild(grid);
}

// ────────────────────────────────────────────────
// Saisons
// ────────────────────────────────────────────────
async function loadSeason(forestId) {
    try {
        const res = await fetch(`${API.FORESTS}/${forestId}/current-season`);
        if (!res.ok) return;

        const season = await res.json();
        const name = season.name || season;

        const panel = document.getElementById('seasonPanel');
        const text = document.getElementById('currentSeasonName');

        if (panel && text) {
            panel.classList.remove('d-none');
            text.textContent = name;

            panel.className = 'p-2 border rounded bg-light';
            if (name === 'WINTER') panel.classList.add('bg-info', 'bg-opacity-25');
            if (name === 'SPRING') panel.classList.add('bg-success', 'bg-opacity-25');
            if (name === 'SUMMER') panel.classList.add('bg-warning', 'bg-opacity-25');
            if (name === 'AUTUMN') panel.classList.add('bg-danger', 'bg-opacity-25');
        }
    } catch {}
}

async function advanceSeason() {
    const forestId = document.getElementById('forestSelect')?.value;
    if (!forestId) return;

    try {
        await fetch(`${API.FORESTS}/${forestId}/advance-season`, { method: 'POST' });
        await loadForestDetails();
    } catch (err) {
        console.error("Erreur advance season", err);
    }
}

// ────────────────────────────────────────────────
// Plantation
// ────────────────────────────────────────────────
function openPlantModal(x, y) {
    currentX = x;
    currentY = y;
    document.getElementById('targetX').textContent = x;
    document.getElementById('targetY').textContent = y;
    document.getElementById('plantNameInput').value = `Plante_${x}_${y}`;

    plantModal = new bootstrap.Modal(document.getElementById('plantSeedModal'));
    plantModal.show();
}

async function confirmPlanting() {
    const forestId = document.getElementById('forestSelect')?.value;
    const speciesId = document.getElementById('speciesSelectModal')?.value;
    const name = document.getElementById('plantNameInput')?.value.trim();

    if (!forestId || !speciesId || !name) {
        alert("Veuillez remplir tous les champs.");
        return;
    }

    try {
        console.log(`[Plant] Tentative → ${name} (espèce ${speciesId}) en (${currentX},${currentY})`);

        // 1. Création plante
        const createRes = await fetch(
            `${API.PLANTS}/create?name=${encodeURIComponent(name)}&speciesId=${speciesId}`,
            { method: 'POST' }
        );

        if (!createRes.ok) {
            const err = await createRes.text();
            throw new Error(`Échec création plante : ${createRes.status} - ${err}`);
        }

        const newPlant = await createRes.json();
        console.log(`[Plant] Créée → ID = ${newPlant.id}`);

        // 2. Placement
        const placeUrl = `${API.FORESTS}/${forestId}/plants/${newPlant.id}?x=${currentX}&y=${currentY}`;
        const placeRes = await fetch(placeUrl, { method: 'POST' });

        if (placeRes.ok) {
            console.log("[Plant] Placement réussi !");
            plantModal?.hide();
            await loadForestDetails(); // refresh immédiat
            setTimeout(loadForestDetails, 600); // double refresh pour être sûr
        } else {
            let errorMsg = "Impossible de placer la plante";
            try {
                const data = await placeRes.json();
                errorMsg += ` → ${data.message || data.error || placeRes.statusText}`;
                if (placeRes.status === 409) {
                    errorMsg += " (la case est déjà occupée dans la base)";
                }
            } catch {
                errorMsg += ` (HTTP ${placeRes.status})`;
            }
            console.warn("[Plant] Échec placement", placeRes.status, placeRes.statusText);
            alert(errorMsg);
            // On supprime la plante créée si placement a échoué (évite les orphelines)
            await fetch(`${API.PLANTS}/${newPlant.id}`, { method: 'DELETE' }).catch(()=>{});
        }
    } catch (err) {
        console.error("[Plant] Erreur complète :", err);
        alert("Erreur technique lors du plantage :\n" + err.message);
    }
}

async function loadSpeciesForModal() {
    try {
        const res = await fetch(API.SPECIES);
        const species = await res.json();
        const select = document.getElementById('speciesSelectModal');
        if (select) {
            select.innerHTML = '<option value="" disabled selected>Choisir une espèce...</option>' +
                species.map(s => `<option value="${s.id}">${s.name} (Eau: ${s.optimalWaterNeeds})</option>`).join('');
        }
    } catch (err) {
        console.error("Erreur load species modal", err);
    }
}

// ────────────────────────────────────────────────
// Auto-play
// ────────────────────────────────────────────────
function toggleAutoPlay() {
    const btn = document.getElementById('btnAutoPlay');
    const forestId = document.getElementById('forestSelect')?.value;

    if (!forestId) return alert("Sélectionnez une forêt d'abord !");

    if (autoPlayInterval) {
        clearInterval(autoPlayInterval);
        autoPlayInterval = null;
        btn.innerHTML = '<i class="fas fa-play"></i> Auto';
        btn.classList.replace('btn-danger', 'btn-outline-danger');
    } else {
        btn.innerHTML = '<i class="fas fa-stop"></i> Stop';
        btn.classList.replace('btn-outline-danger', 'btn-danger');

        advanceSeason(); // tout de suite
        autoPlayInterval = setInterval(advanceSeason, 2200);
    }
}

// ────────────────────────────────────────────────
// Gestion plante (modal actions)
// ────────────────────────────────────────────────
async function openPlantActionModal(plant) {
    selectedPlantId = plant.id;

    document.getElementById('plantActionTitle').textContent = plant.name;
    document.getElementById('pSpecies').textContent = plant.species?.name || '?';
    document.getElementById('pState').textContent = plant.state || 'Inconnu';
    document.getElementById('pStress').textContent = plant.stressIndex ?? 0;

    try {
        const res = await fetch(API.EFFECTS);
        const effects = await res.json();
        const select = document.getElementById('effectSelector');
        select.innerHTML = '<option selected disabled>Choisir un effet...</option>' +
            effects.map(e => `<option value="${e.id}">${e.name} (${e.modifierValue}%)</option>`).join('');

        // Effets actifs (si le backend les renvoie)
        const list = document.getElementById('activeEffectsList');
        list.innerHTML = '';
        if (plant.activeEffects?.length > 0) {
            plant.activeEffects.forEach(ae => {
                const name = ae.effect?.name || ae.name || 'Effet inconnu';
                list.innerHTML += `<li class="list-group-item">${name}</li>`;
            });
        } else {
            list.innerHTML = '<li class="list-group-item text-muted">Aucun effet actif</li>';
        }
    } catch (err) {
        console.error("Erreur chargement effets", err);
    }

    actionModal = new bootstrap.Modal(document.getElementById('plantActionModal'));
    actionModal.show();
}

async function applyEffectToPlant() {
    const effectId = document.getElementById('effectSelector')?.value;
    if (!effectId || !selectedPlantId) return;

    try {
        const res = await fetch(`/api/plants/${selectedPlantId}/effects/${effectId}`, { method: 'POST' });
        if (res.ok) {
            alert("Effet appliqué ✓");
            actionModal?.hide();
            await loadForestDetails();
        } else {
            alert("Échec application effet");
        }
    } catch (err) {
        console.error(err);
    }
}

async function deletePlantFromForest() {
    if (!confirm("Arracher cette plante définitivement ?")) return;

    try {
        await fetch(`${API.PLANTS}/${selectedPlantId}`, { method: 'DELETE' });
        actionModal?.hide();
        await loadForestDetails();
    } catch (err) {
        alert("Erreur lors de l'arrachage");
    }
}

