// CONFIGURATION DES ENDPOINTS API
// Adaptez si nécessaire (ex: si /plants devient /api/plants dans le futur)
const API = {
    FORESTS: '/api/forests',
    PLANTS: '/plants',
    SPECIES: '/api/species',
    SEASONS: '/api/seasons' // Si applicable
};

// Initialisation
document.addEventListener('DOMContentLoaded', async () => {
    // 1. Charger les listes normales
    await loadForests();
    loadSpeciesForModal();

    // 2. Vérifier si c'est vide et créer des DONNÉES DE DÉMO si besoin
    await checkAndCreateDemoData();
});

/* --- FONCTION DE DONNEES DE DEMO --- */
async function checkAndCreateDemoData() {
    try {
        // Vérifier les forêts
        const resF = await fetch(API.FORESTS);
        const forests = await resF.json();

        if (forests.length === 0) {
            console.log("Aucune forêt trouvée, création des données de démo...");
            
            // A. Créer une Espèce de test
            const speciesData = {
                name: "Chêne Royal (Demo)",
                optimalWaterNeeds: 500,
                optimalTemperature: 20,
                optimalLuxNeeds: 1000,
                baseGrowthRate: 1.2
            };
            const resSpec = await fetch('/api/species', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(speciesData)
            });
            const demoSpecies = await resSpec.json();

            // B. Créer une Forêt
            const forestData = { name: "Forêt de Démonstration", width: 8, height: 6 };
            const resFor = await fetch(API.FORESTS, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(forestData)
            });
            const demoForest = await resFor.json();

            // C. Créer une Plante
            const plantRes = await fetch(`${API.PLANTS}/create?name=Arbre_Ancien&speciesId=${demoSpecies.id}`, {method: 'POST'});
            const demoPlant = await plantRes.json();

            // D. Ajouter la plante à la forêt (Position 3, 3)
            await fetch(`${API.FORESTS}/${demoForest.id}/plants/${demoPlant.id}?x=3&y=3`, {method: 'POST'});

            // E. Créer un Effet de test (Engrais)
            await fetch('/api/effects', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({name: "Engrais Magique (Demo)", type: "BENEFICIAL", modifierValue: 50})
            });

            // Rafraîchir l'interface
            alert("✨ Bienvenue ! Une forêt de démonstration a été créée pour vous.");
            loadForests(demoForest.id); // Sélectionner la nouvelle forêt
        }
    } catch (e) {
        console.error("Erreur création demo data", e);
    }
}

// --- GESTION DES FORETS ---

async function loadForests(idToSelect = null) {
    try {
        const res = await fetch(API.FORESTS);
        const forests = await res.json();
        const select = document.getElementById('forestSelect');
        
        // Sauvegarder la valeur actuelle si on n'a pas forcé une sélection
        const currentVal = idToSelect || select.value;
        
        // Vider et remplir la liste
        select.innerHTML = '<option value="" disabled selected>-- Choisir --</option>';
        forests.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.id;
            opt.text = `${f.name} (${f.width}x${f.height})`;
            select.appendChild(opt);
        });

        // Si on a un ID à sélectionner (soit le nouveau, soit l'ancien), on le remet
        // On vérifie que cet ID existe bien dans la nouvelle liste
        if(currentVal && forests.some(f => f.id === currentVal)) {
            select.value = currentVal;
            // Charge aussi la grille automatiquement
            loadForestDetails();
        }

    } catch (e) {
        console.error("Erreur chargement forêts:", e);
    }
}

// 2. Fonction de création qui rappelle le chargement à la fin
async function createForest() {
    // Récupération des valeurs du formulaire (Modal)
    const name = document.getElementById('newForestName').value;
    const w = document.getElementById('newForestW').value;
    const h = document.getElementById('newForestH').value;

    if (!name) return alert("Le nom est obligatoire");

    try {
        // Envoi de la requête au Backend
        const res = await fetch(API.FORESTS, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: name, width: parseInt(w), height: parseInt(h) })
        });

        if (res.ok) {
            // IMPORTANT : On récupère la forêt créée pour avoir son ID
            const newForest = await res.json();

            // 1. Fermer le modal Bootstrap proprement
            const modalEl = document.getElementById('createForestModal');
            const modal = bootstrap.Modal.getInstance(modalEl);
            modal.hide();

            // 2. Vider les champs pour la prochaine fois
            document.getElementById('newForestName').value = "";
            
            // 3. Recharger la liste ET sélectionner la nouvelle forêt
            await loadForests(newForest.id);
            
            // Petit feedback visuel
            alert(`Forêt "${newForest.name}" créée et chargée !`);
        } else {
            alert("Erreur lors de la création.");
        }
    } catch (e) {
        console.error(e);
        alert("Erreur technique lors de la création.");
    }
}

async function deleteForest() {
    const id = document.getElementById('forestSelect').value;
    if (!id) return;
    if (!confirm("Voulez-vous vraiment supprimer cette forêt et toutes ses plantes ?")) return;

    await fetch(`${API.FORESTS}/${id}`, { method: 'DELETE' });
    document.getElementById('gridContainer').innerHTML = '';
    document.getElementById('seasonPanel').classList.add('d-none');
    loadForests();
}

// --- AFFICHAGE ET GRILLE ---

async function loadForestDetails() {
    const forestId = document.getElementById('forestSelect').value;
    if (!forestId) return;

    // 1. Récupérer données Forêt
    // Astuce: On recharge la liste pour trouver les dimensions de la forêt sélectionnée
    // Si votre API a un endpoint GET /forests/{id}, utilisez-le plutôt.
    const resAll = await fetch(API.FORESTS);
    const all = await resAll.json();
    const forest = all.find(f => f.id === forestId);

    // 2. Récupérer les plantes de la forêt
    const resPlants = await fetch(`${API.FORESTS}/${forestId}/plants`);
    const plants = await resPlants.json();

    // 3. Récupérer la saison
    loadSeason(forestId);

    // 4. Mettre à jour titre
    document.getElementById('forestTitle').innerText = `${forest.name} - ${plants.length} plante(s)`;
    document.getElementById('gridLegend').classList.remove('d-none');

    // 5. Dessiner la grille
    drawGrid(forest, plants);
}

function drawGrid(forest, plants) {
    const container = document.getElementById('gridContainer');
    container.innerHTML = '';

    const grid = document.createElement('div');
    grid.className = 'forest-grid';
    // Configuration dynamique des colonnes CSS
    grid.style.gridTemplateColumns = `repeat(${forest.width}, 50px)`;

    for (let y = 0; y < forest.height; y++) {
        for (let x = 0; x < forest.width; x++) {
            const cell = document.createElement('div');
            cell.className = 'forest-cell';

            // Chercher si une plante existe à cette position (x, y)
            const plant = plants.find(p => p.position && p.position.x === x && p.position.y === y);

            if (plant) {
                // CASE OCCUPÉE
                cell.classList.add('occupied', `status-${plant.state}`);
                cell.innerHTML = '<i class="fas fa-seedling"></i>';
                cell.title = `${plant.name}\nEspèce: ${plant.species ? plant.species.name : '?'}\nÉtat: ${plant.state}`;
                
                // Clic sur plante : voir détails
                // Ouvre le modal de contrôle au lieu d'une simple alerte
                cell.onclick = () => openPlantActionModal(plant);
            } else {
                // CASE VIDE
                cell.title = `Vide (${x}, ${y})`;
                cell.onclick = () => openPlantModal(x, y);
            }

            grid.appendChild(cell);
        }
    }
    container.appendChild(grid);
}

// --- SAISONS ---

async function loadSeason(forestId) {
    try {
        const res = await fetch(`${API.FORESTS}/${forestId}/current-season`);
        if(res.ok) {
            const season = await res.json();
            document.getElementById('seasonPanel').classList.remove('d-none');
            // Gérer si le retour est un objet ou une string enum
            const seasonName = season.name || season; 
            document.getElementById('currentSeasonName').innerText = seasonName;
            
            // Mise à jour couleur badge selon saison
            const panel = document.getElementById('seasonPanel');
            panel.className = `p-2 border rounded bg-light text-dark`; // Reset
            if(seasonName === 'WINTER') panel.classList.add('bg-info', 'bg-opacity-25');
            if(seasonName === 'SUMMER') panel.classList.add('bg-warning', 'bg-opacity-25');
            if(seasonName === 'SPRING') panel.classList.add('bg-success', 'bg-opacity-25');
            if(seasonName === 'AUTUMN') panel.classList.add('bg-danger', 'bg-opacity-25');
        }
    } catch (e) {
        console.log("Pas de saison active");
    }
}

async function advanceSeason() {
    const forestId = document.getElementById('forestSelect').value;
    if(!forestId) return;

    await fetch(`${API.FORESTS}/${forestId}/advance-season`, { method: 'POST' });
    loadForestDetails(); // Recharger tout pour voir les impacts sur les plantes
}

// --- PLANTATION (Logique en 2 étapes) ---

let currentX, currentY;
let plantModal;

function openPlantModal(x, y) {
    currentX = x;
    currentY = y;
    document.getElementById('targetX').innerText = x;
    document.getElementById('targetY').innerText = y;
    document.getElementById('plantNameInput').value = `Plante_${x}_${y}`; // Nom par défaut
    
    plantModal = new bootstrap.Modal(document.getElementById('plantSeedModal'));
    plantModal.show();
}

async function confirmPlanting() {
    const forestId = document.getElementById('forestSelect').value;
    const speciesId = document.getElementById('speciesSelectModal').value;
    const name = document.getElementById('plantNameInput').value;

    if (!speciesId) return alert("Il faut choisir une espèce !");

    // ÉTAPE 1: Créer la plante (POST /plants/create)
    // URL paramétrée selon ton README
    const createUrl = `${API.PLANTS}/create?name=${encodeURIComponent(name)}&speciesId=${speciesId}`;
    
    try {
        const resCreate = await fetch(createUrl, { method: 'POST' });
        if (!resCreate.ok) throw new Error("Erreur création plante");
        
        const newPlant = await resCreate.json();
        
        // ÉTAPE 2: Ajouter la plante à la forêt (POST /api/forests/{id}/plants/{plantId}?x=..&y=..)
        const addUrl = `${API.FORESTS}/${forestId}/plants/${newPlant.id}?x=${currentX}&y=${currentY}`;
        const resAdd = await fetch(addUrl, { method: 'POST' });

        if (resAdd.ok) {
            plantModal.hide();
            loadForestDetails(); // Rafraîchir la grille
        } else {
            alert("Erreur: Impossible de placer la plante (Case occupée ?)");
        }
    } catch (e) {
        alert("Erreur technique: " + e.message);
    }
}

async function loadSpeciesForModal() {
    const res = await fetch(API.SPECIES);
    const list = await res.json();
    const select = document.getElementById('speciesSelectModal');
    select.innerHTML = list.map(s => `<option value="${s.id}">${s.name} (Eau: ${s.optimalWaterNeeds})</option>`).join('');
}

// --- AFFICHAGE ESPECES ---

async function loadSpecies() {
    const res = await fetch(API.SPECIES);
    const list = await res.json();
    const container = document.getElementById('speciesList');
    
    container.innerHTML = list.map(s => `
        <div class="col-md-4">
            <div class="card h-100 border-success">
                <div class="card-header bg-success text-white fw-bold">${s.name}</div>
                <div class="card-body small">
                    <ul class="list-unstyled">
                        <li>💧 Eau: ${s.optimalWaterNeeds}</li>
                        <li>☀️ Lumière: ${s.optimalLuxNeeds}</li>
                        <li>🌡️ Temp: ${s.optimalTemperature}°C</li>
                        <li>📈 Croissance: ${s.baseGrowthRate}</li>
                    </ul>
                </div>
            </div>
        </div>
    `).join('');
}

// --- INVENTAIRE GLOBAL ---

async function loadAllPlants() {
    const res = await fetch(API.PLANTS);
    const list = await res.json();
    const tbody = document.getElementById('allPlantsTable');
    
    tbody.innerHTML = list.map(p => `
        <tr>
            <td><small class="text-muted">${p.id.substring(0,8)}...</small></td>
            <td>${p.name}</td>
            <td>${p.species ? p.species.name : '-'}</td>
            <td><span class="badge ${getStateBadgeClass(p.state)}">${p.state}</span></td>
        </tr>
    `).join('');
}

function getStateBadgeClass(state) {
    if(state === 'HEALTHY') return 'bg-success';
    if(state === 'STRESSED') return 'bg-warning text-dark';
    if(state === 'DISEASED') return 'bg-danger';
    return 'bg-secondary';
}
/* --- AUTO PLAY FEATURE --- */
let autoPlayInterval = null;

function toggleAutoPlay() {
    const btn = document.getElementById('btnAutoPlay');
    
    if (autoPlayInterval) {
        // STOP
        clearInterval(autoPlayInterval);
        autoPlayInterval = null;
        btn.innerHTML = '<i class="fas fa-play"></i> Auto';
        btn.classList.replace('btn-danger', 'btn-outline-danger');
    } else {
        // START (Toutes les 2 secondes)
        const forestId = document.getElementById('forestSelect').value;
        if(!forestId) return alert("Sélectionnez une forêt d'abord !");

        btn.innerHTML = '<i class="fas fa-stop"></i> Stop';
        btn.classList.replace('btn-outline-danger', 'btn-danger');

        // Lance l'évolution toutes les 2s
        advanceSeason(); // Une fois tout de suite
        autoPlayInterval = setInterval(() => {
            advanceSeason();
        }, 2000); 
    }
}

/* --- GESTION DU MODAL D'ACTION PLANTE --- */

let selectedPlantId = null;
let actionModal = null;

async function openPlantActionModal(plant) {
    selectedPlantId = plant.id;
    
    // Remplir les infos
    document.getElementById('plantActionTitle').innerText = plant.name;
    document.getElementById('pSpecies').innerText = plant.species ? plant.species.name : '?';
    document.getElementById('pState').innerText = plant.state;
    document.getElementById('pStress').innerText = plant.stressIndex || 0;

    // Charger la liste des effets disponibles pour le select
    const res = await fetch(API.EFFECTS);
    const effects = await res.json();
    const select = document.getElementById('effectSelector');
    select.innerHTML = '<option selected disabled>Choisir un effet...</option>';
    effects.forEach(e => {
        select.innerHTML += `<option value="${e.id}">${e.name} (${e.modifierValue}%)</option>`;
    });

    // Afficher les effets actifs de la plante (s'il y en a)
    // Note: Cela dépend si votre API renvoie "activeEffects" dans l'objet plante
    const list = document.getElementById('activeEffectsList');
    list.innerHTML = '';
    if(plant.activeEffects && plant.activeEffects.length > 0) {
        plant.activeEffects.forEach(ae => {
            // Adaptez selon la structure de votre JSON (ae.effect.name ou ae.name)
            list.innerHTML += `<li class="list-group-item">${ae.effect ? ae.effect.name : 'Effet inconnu'}</li>`;
        });
    } else {
        list.innerHTML = '<li class="list-group-item text-muted font-italic">Aucun effet actif</li>';
    }

    // Ouvrir le modal
    actionModal = new bootstrap.Modal(document.getElementById('plantActionModal'));
    actionModal.show();
}

async function applyEffectToPlant() {
    const effectId = document.getElementById('effectSelector').value;
    if(!effectId || !selectedPlantId) return;

    // API Call: POST /api/plants/{plantId}/effects/{effectId}
    // Adaptez l'URL selon votre README (votre README dit /api/plants/PLANT_ID/effects/EFFECT_ID)
    try {
        const url = `/api/plants/${selectedPlantId}/effects/${effectId}`;
        const res = await fetch(url, { method: 'POST' });
        
        if(res.ok) {
            alert("Effet appliqué !");
            actionModal.hide();
            loadForestDetails(); // Rafraîchir la grille pour voir les changements
        } else {
            alert("Erreur lors de l'application de l'effet.");
        }
    } catch(e) {
        console.error(e);
    }
}

async function deletePlantFromForest() {
    if(!confirm("Arracher cette plante définitivement ?")) return;
    
    // Suppression
    await fetch(`${API.PLANTS}/${selectedPlantId}`, { method: 'DELETE' });
    
    actionModal.hide();
    loadForestDetails();
}