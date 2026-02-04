const API_EFFECTS = '/api/effects';

document.addEventListener('DOMContentLoaded', loadEffects);

async function loadEffects() {
    const res = await fetch(API_EFFECTS);
    const list = await res.json();
    const container = document.getElementById('effectsList');
    
    container.innerHTML = list.map(e => `
        <div class="col-md-4">
            <div class="card h-100 shadow-sm border-start border-5 ${e.type === 'BENEFICIAL' ? 'border-success' : 'border-danger'}">
                <div class="card-body">
                    <h5 class="card-title fw-bold">${e.name}</h5>
                    <span class="badge ${e.type === 'BENEFICIAL' ? 'bg-success' : 'bg-danger'} mb-2">${e.type}</span>
                    <p class="card-text">Modificateur: <strong>${e.modifierValue}%</strong></p>
                    <button class="btn btn-sm btn-outline-secondary w-100" onclick="deleteEffect('${e.id}')">Supprimer</button>
                </div>
            </div>
        </div>
    `).join('');
}

async function createEffect() {
    const data = {
        name: document.getElementById('effName').value,
        type: document.getElementById('effType').value,
        modifierValue: parseFloat(document.getElementById('effMod').value)
    };

    if(!data.name) return alert("Nom obligatoire");

    await fetch(API_EFFECTS, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });
    
    // Fermer modal et recharger
    const modal = bootstrap.Modal.getInstance(document.getElementById('createEffectModal'));
    modal.hide();
    loadEffects();
}

async function deleteEffect(id) {
    if(confirm('Supprimer ?')) {
        await fetch(`${API_EFFECTS}/${id}`, {method: 'DELETE'});
        loadEffects();
    }
}