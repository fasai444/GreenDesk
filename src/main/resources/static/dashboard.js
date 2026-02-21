const API_BASE = '/api'; // Ou '' selon votre config
const URLS = {
    SPECIES: '/api/species',
    PLANTS: '/plants',
    FORESTS: '/api/forests'
};

document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();
});

async function loadDashboardData() {
    try {
        // 1. Lancer les 3 requêtes en parallèle pour la vitesse
        const [resSpecies, resPlants, resForests] = await Promise.all([
            fetch(URLS.SPECIES),
            fetch(URLS.PLANTS),
            fetch(URLS.FORESTS)
        ]);

        const species = await resSpecies.json();
        const plants = await resPlants.json();
        const forests = await resForests.json();

        // 2. Mettre à jour les Compteurs (Cartes du haut)
        document.getElementById('totalSpecies').innerText = species.length;
        document.getElementById('totalPlants').innerText = plants.length;
        document.getElementById('totalForests').innerText = forests.length;

        // 3. Calculer le Stress Moyen
        if (plants.length > 0) {
            // Suppose que chaque plante a un champ stressIndex
            const totalStress = plants.reduce((sum, p) => sum + (p.stressIndex || 0), 0);
            const avg = Math.round(totalStress / plants.length);
            document.getElementById('avgStress').innerText = `${avg}%`;
            
            // Couleur dynamique
            const stressEl = document.getElementById('avgStress');
            stressEl.className = `fw-bold mb-0 ${avg > 50 ? 'text-danger' : 'text-dark'}`;
        }

        // 4. Remplir la liste "Plantes Récentes" (Les 4 dernières)
        const recentList = document.getElementById('recentPlantsList');
        recentList.innerHTML = '';
        // On prend les derniers éléments du tableau
        const lastPlants = plants.slice(-4).reverse(); 
        
        lastPlants.forEach(p => {
            const html = `
                <div class="d-flex align-items-center justify-content-between border-bottom pb-2">
                    <div class="d-flex align-items-center">
                        <div class="bg-light p-2 rounded me-3 text-success"><i class="fas fa-leaf"></i></div>
                        <div>
                            <h6 class="mb-0 fw-bold">${p.name}</h6>
                            <small class="text-muted">${p.species ? p.species.name : 'Inconnu'}</small>
                        </div>
                    </div>
                    <span class="badge ${getBadgeColor(p.state)}">${p.state}</span>
                </div>
            `;
            recentList.innerHTML += html;
        });

        // 5. Remplir la liste "Forêts"
        const forestList = document.getElementById('forestStatusList');
        forestList.innerHTML = '';
        forests.forEach(f => {
             const html = `
                <div class="d-flex align-items-center justify-content-between border-bottom pb-2">
                    <div>
                        <h6 class="mb-0 fw-bold">${f.name}</h6>
                        <small class="text-muted">Dimensions: ${f.width}x${f.height}</small>
                    </div>
                    <div class="bg-light px-2 py-1 rounded border">
                        <small class="fw-bold">${f.width * f.height}m²</small>
                    </div>
                </div>
            `;
            forestList.innerHTML += html;
        });

    } catch (e) {
        console.error("Erreur chargement dashboard", e);
    }
}

function getBadgeColor(state) {
    if(state === 'HEALTHY') return 'bg-success bg-opacity-10 text-success'; // Vert clair style moderne
    if(state === 'STRESSED') return 'bg-warning bg-opacity-10 text-warning';
    if(state === 'DISEASED') return 'bg-danger bg-opacity-10 text-danger';
    return 'bg-secondary bg-opacity-10 text-secondary';
}

// =========================================================
// AJOUTS POUR LIVRAISON 3 (F1 & F2)
// =========================================================

/**
 * FEATURE L3-F1 : Création d'un effet personnalisé
 */
async function createCustomEffect(event) {
    event.preventDefault(); 

    const effectData = {
        name: document.getElementById('effectName').value,
        durationInHours: parseInt(document.getElementById('effectDuration').value), // Vérifie si ton backend attend durationHours ou durationInHours
        temperatureModifier: parseFloat(document.getElementById('effectTemp').value) || 0,
        waterModifier: parseFloat(document.getElementById('effectWater').value) || 0,
        isCustom: true 
    };

    try {
        const response = await fetch('/api/effects', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(effectData)
        });

        if (response.ok) {
            alert("🧪 Nouvel effet créé avec succès !");
            document.getElementById('createEffectForm').reset();
        } else {
            const error = await response.text();
            alert("Erreur : " + error);
        }
    } catch (e) {
        console.error("Erreur création effet", e);
    }
}
/**
 * FEATURE L3-F2 : Appliquer un Stimulus à une forêt
 * @param {string} type - 'HEATWAVE' ou 'RAIN'
 */
a/**
 * FEATURE L3-F2 : Envoi d'un stimulus climatique
 * @param {string} type - 'HEATWAVE' ou 'RAIN'
 */
async function sendStimulus(type) {
    // 1. Récupération de la forêt sélectionnée
    const forestId = document.getElementById('forestSelect').value;
    
    if (!forestId) {
        return alert("⚠️ Veuillez sélectionner une forêt dans la liste avant de lancer un stimulus.");
    }

    const stimulus = {
        type: type,
        forestId: forestId,
        intensity: type === 'HEATWAVE' ? 45.0 : 20.0,
        durationHours: 12
    };

    try {
        const response = await fetch('/api/stimuli', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(stimulus)
        });

        if (response.ok) {
            alert(`⚡ Succès : Stimulus ${type} appliqué à la forêt !`);
            // On rafraîchit les données pour voir l'impact sur le stress
            loadDashboardData(); 
        } else {
            alert("❌ Erreur lors de l'envoi du stimulus.");
        }
    } catch (error) {
        console.error("Erreur API Stimulus:", error);
    }
}

/**
 * CORRECTION PHOTO 2 : Gestion du badge de statut
 * Remplace l'ancien affichage par celui-ci pour éviter le 'undefined'
 */
function renderPlantBadge(plant) {
    // Vérification du nom du champ renvoyé par le backend (Swagger montre 'state')
    // On ajoute une valeur de repli 'N/A' si le champ est manquant
    const statusText = plant.state || plant.status || "N/A";
    
    let badgeClass = "bg-secondary"; // Par défaut
    if (statusText === "HEALTHY") badgeClass = "bg-success";
    if (statusText === "STRESSED") badgeClass = "bg-warning text-dark";
    if (statusText === "DISEASED") badgeClass = "bg-danger";

    return `<span class="badge ${badgeClass} bg-opacity-10 text-dark border">${statusText}</span>`;
}

/**
 * FEATURE L3-F2 : Cloner une plante (Outil de comparaison)
 */
async function clonePlant(plantId) {
    const targetForestId = prompt("Entrez l'ID de la forêt cible pour le clonage :");
    if (!targetForestId) return;

    try {
        // On clone aux coordonnées 0,0 par défaut pour ce test
        const response = await fetch(`/api/plants/${plantId}/clone?targetForestId=${targetForestId}&x=0&y=0`, {
            method: 'POST'
        });

        if (response.ok) {
            alert("👥 Plante clonée avec succès ! Vérifiez la forêt cible.");
            loadDashboardData();
        } else {
            alert("Erreur lors du clonage.");
        }
    } catch (e) {
        console.error("Erreur clonage", e);
    }
}

/**
 * FEATURE L3-F2 : Afficher le rapport de diagnostic détaillé
 */
async function showPlantStatus(plantId) {
    try {
        const response = await fetch(`/plants/${plantId}/status`);
        const data = await response.json();

        // On remplit une zone de l'interface (assure-toi d'avoir ces IDs dans ton HTML)
        const content = document.getElementById('statusContent');
        content.innerHTML = `
            <div class="p-3 border rounded bg-light">
                <h5 class="fw-bold">${data.name} <span class="badge ${getBadgeColor(data.state)}">${data.state}</span></h5>
                <p class="mb-1"><strong>Forêt :</strong> ${data.forestId}</p>
                <div class="progress mb-2" style="height: 10px;">
                    <div class="progress-bar bg-warning" style="width: ${data.stressIndex * 100}%"></div>
                </div>
                <small>Index de stress : ${(data.stressIndex * 100).toFixed(1)}%</small>
                <hr>
                <div class="row text-center">
                    <div class="col-4">
                        <i class="fas fa-thermometer-half"></i><br>
                        <small>${data.sensors.temperature}°C</small>
                    </div>
                    <div class="col-4">
                        <i class="fas fa-tint"></i><br>
                        <small>${data.sensors.water}mL</small>
                    </div>
                    <div class="col-4">
                        <i class="fas fa-lightbulb"></i><br>
                        <small>${data.sensors.lux}lx</small>
                    </div>
                </div>
            </div>
        `;
        document.getElementById('statusReport').style.display = 'block';
    } catch (e) {
        console.error("Erreur diagnostic", e);
    }
}

// Lier le formulaire d'effet si le DOM est chargé
document.addEventListener('submit', (e) => {
    if (e.target.id === 'createEffectForm') createCustomEffect(e);
});


// --- 1. CHARGEMENT DES FORÊTS DANS LE MENU ---
async function loadForestsForSelect() {
    try {
        const response = await fetch('/api/forests');
        const forests = await response.json();
        const select = document.getElementById('forestSelect');
        
        if (select) {
            // Réinitialisation propre
            select.innerHTML = '<option value="" disabled selected>Choisir une forêt...</option>';
            
            forests.forEach(f => {
                const option = document.createElement('option');
                option.value = f.id;
                option.textContent = f.name; // Utilise textContent ici
                select.appendChild(option);
            });
        }
    } catch (e) {
        console.error("Erreur chargement select forests:", e);
    }
}

// --- 2. GESTION DES COULEURS DE STATUT ---
function getBadgeColor(state) {
    // On s'assure que state est en majuscules pour correspondre à l'Enum Java
    const status = (state || "UNKNOWN").toUpperCase(); 
    
    if(status === 'HEALTHY') return 'bg-success bg-opacity-10 text-success';
    if(status === 'STRESSED') return 'bg-warning bg-opacity-10 text-warning';
    if(status === 'DISEASED') return 'bg-danger bg-opacity-10 text-danger';
    
    return 'bg-secondary bg-opacity-10 text-secondary';
}

// --- 3. INITIALISATION (Lien avec le DOM) ---
document.addEventListener('DOMContentLoaded', () => {
    // On charge les données globales
    if (typeof loadDashboardData === "function") loadDashboardData(); 
    
    // On remplit le menu déroulant
    loadForestsForSelect();

    // On lie le formulaire du laboratoire (L3-F1)
    const effectForm = document.getElementById('createEffectForm');
    if (effectForm) {
        // On s'assure que createCustomEffect existe bien
        effectForm.addEventListener('submit', createCustomEffect);
    }
});

// Fonction pour créer un effet personnalisé
async function createCustomEffect(event) {
    event.preventDefault(); // Empêche le rechargement de la page

    const effectData = {
        name: document.getElementById('effectName').value,
        durationInHours: parseInt(document.getElementById('effectDuration').value),
        temperatureModifier: parseFloat(document.getElementById('effectTemp').value) || 0,
        isCustom: true // Flag important pour votre livraison
    };

    try {
        const response = await fetch('/api/effects', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(effectData)
        });

        if (response.ok) {
            alert("🧪 Nouvel effet créé avec succès !");
            event.target.reset(); // Vide le formulaire
        }
    } catch (e) {
        console.error("Erreur technique lors de la création", e);
    }
}

// Liaison avec le formulaire (ID à vérifier dans votre HTML)
document.getElementById('createEffectForm').addEventListener('submit', createCustomEffect);

async function loadForestsForSelect() {
    const res = await fetch('/api/forests');
    const forests = await res.json();
    const select = document.getElementById('forestSelect');
    
    // On remplit le <select> dynamiquement
    select.innerHTML = '<option value="" disabled selected>Choisir une forêt...</option>';
    forests.forEach(f => {
        select.innerHTML += `<option value="${f.id}">${f.name}</option>`;
    });
}

async function sendStimulus(type) {
    const forestId = document.getElementById('forestSelect').value;
    if (!forestId) return alert("Veuillez d'abord sélectionner une forêt !");

    const stimulus = {
        type: type, // 'HEATWAVE' ou 'RAIN'
        forestId: forestId,
        intensity: 50.0,
        durationHours: 12
    };

    const response = await fetch('/api/stimuli', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(stimulus)
    });

    if (response.ok) {
        alert(`⚡ Stimulus ${type} envoyé avec succès sur la forêt !`);
    }
}