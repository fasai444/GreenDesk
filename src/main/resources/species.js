const API_SPECIES = '/api/species';

document.addEventListener('DOMContentLoaded', loadSpeciesTable);

async function loadSpeciesTable() {
    const res = await fetch(API_SPECIES);
    const list = await res.json();
    const tbody = document.getElementById('speciesTableBody');
    
    tbody.innerHTML = list.map(s => `
        <tr>
            <td class="fw-bold">${s.name}</td>
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

async function createSpecies() {
    const data = {
        name: document.getElementById('spName').value,
        optimalWaterNeeds: parseFloat(document.getElementById('spWater').value),
        optimalTemperature: parseFloat(document.getElementById('spTemp').value),
        optimalLuxNeeds: parseFloat(document.getElementById('spLux').value),
        optimalHumidity: parseFloat(document.getElementById('spHum').value),
        baseGrowthRate: 1.0,
        seedProductionRate: 0.5
    };

    if(!data.name) return alert("Le nom est obligatoire");

    const res = await fetch(API_SPECIES, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });

    if(res.ok) {
        // Fermer modal et recharger
        const modal = bootstrap.Modal.getInstance(document.getElementById('createSpeciesModal'));
        modal.hide();
        document.getElementById('spName').value = "";
        loadSpeciesTable();
    } else {
        alert("Erreur création");
    }
}

async function deleteSpecies(id) {
    if(confirm("Supprimer cette espèce ?")) {
        await fetch(`${API_SPECIES}/${id}`, {method: 'DELETE'});
        loadSpeciesTable();
    }
}