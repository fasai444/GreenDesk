async function tick() {
    await fetch('/api/ecosystem/tick', { method: 'POST' });
    await loadGrid();
}

async function simulate() {
    const n = document.getElementById("nbTicks").value;
    await fetch(`/api/ecosystem/simulate/${n}`, { method: 'POST' });
    await loadGrid();
}

async function loadGrid() {
    const res = await fetch('/api/ecosystem/cells');
    const cells = await res.json();

    const grid = document.getElementById("grid");
    grid.innerHTML = "";

    let maxX = 0, maxY = 0;
    cells.forEach(line => {
        const m = line.match(/Cell \[(\d+),(\d+)\]/);
        if (m) {
            maxX = Math.max(maxX, parseInt(m[1]));
            maxY = Math.max(maxY, parseInt(m[2]));
        }
    });

    grid.style.gridTemplateColumns = `repeat(${maxX + 1}, 42px)`;

    const map = {};
    cells.forEach(line => {
        const m = line.match(/Cell \[(\d+),(\d+)\] Plant: (.+) \| Disease: (.+) \| Severity: (.+)/);
        if (!m) return;
        map[`${m[1]},${m[2]}`] = {
            x: m[1], y: m[2],
            plantId: m[3],
            disease: m[4],
            severity: m[5]
        };
    });

    for (let y = 0; y <= maxY; y++) {
        for (let x = 0; x <= maxX; x++) {
            const key = `${x},${y}`;
            const cell = document.createElement("div");
            cell.classList.add("cell");

            const data = map[key];

            if (!data || data.plantId === "null") {
                cell.classList.add("empty");
            } else if (data.disease !== "Healthy") {
                cell.classList.add("diseased");
                cell.classList.add("pulse");
            } else {
                cell.classList.add("healthy");
            }

            cell.onclick = () => showDetails(data);
            grid.appendChild(cell);
        }
    }
}

function showDetails(cell) {
    const panel = document.getElementById("cellDetails");
    if (!cell) {
        panel.textContent = "Cellule vide";
        return;
    }

    panel.textContent =
`Position : (${cell.x}, ${cell.y})
Plante ID : ${cell.plantId}
État : ${cell.disease}
Sévérité : ${cell.severity}`;
}

loadGrid();