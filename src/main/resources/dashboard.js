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