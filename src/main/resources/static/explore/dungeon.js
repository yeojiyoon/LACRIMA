const gridEl = document.getElementById("grid");
const statusEl = document.getElementById("status");
const toastEl = document.getElementById("toast");
const btnReload = document.getElementById("btnReload");

const mapIdEl = document.getElementById("mapId");
const characterIdEl = document.getElementById("characterId");

let state = null; // ExploreStateResponse

function mapId() { return Number(mapIdEl.value); }
function characterId() { return Number(characterIdEl.value); }

function toast(msg) {
    toastEl.textContent = msg || "";
}

function isAdjacent(x1, y1, x2, y2) { //인접?
    return Math.abs(x1 - x2) + Math.abs(y1 - y2) === 1;
}

function tileKey(x, y) {
    return `${x},${y}`;
}

function tilesToMap(tiles) {
    const m = new Map();
    for (const t of tiles || []) m.set(tileKey(t.x, t.y), t);
    return m;
}

async function apiGetState() { //맵 불러오기...
    const url = `/api/explore/state?mapId=${mapId()}&characterId=${characterId()}`;
    const res = await fetch(url);
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || "state 실패");
    return json;
}

async function apiStart(x, y) {
    const url = `/api/explore/start?mapId=${mapId()}&characterId=${characterId()}`;
    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ x, y })
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || "start 실패");
    return json;
}

async function apiMove(x, y) {
    const url = `/api/explore/move?mapId=${mapId()}&characterId=${characterId()}`;
    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ x, y })
    });
    const json = await res.json();
    if (!res.ok) throw new Error(json.message || "move 실패");
    return json;
}

function render() {
    gridEl.innerHTML = "";
    toast("");

    if (!state) {
        statusEl.textContent = "로딩 중...";
        return;
    }

    statusEl.textContent =
        `AP=${state.ap} | pos=(${state.posX},${state.posY}) | started=${state.started}` +
        (state.lastEventType ? ` | EVENT=${state.lastEventType}(${state.lastEventId})` : "");

    const tmap = tilesToMap(state.tiles);

    for (let y = 1; y <= state.mapHeight; y++) {
        for (let x = 1; x <= state.mapWidth; x++) {
            const btn = document.createElement("button");
            btn.className = "cell";
            btn.textContent = `${x}.${y}`;

            const t = tmap.get(tileKey(x, y));
            if (t && t.discovered) btn.classList.add("discovered");

            // 현재 위치 표시(시작 후)
            if (state.started && state.posX === x && state.posY === y) {
                btn.classList.add("me");
            }

            // 시작 후에는 인접만 강조 + 그 외 비활성(UX)
            if (state.started) {
                const adj = isAdjacent(state.posX, state.posY, x, y);
                if (adj) btn.classList.add("adj");
                else if (!(state.posX === x && state.posY === y)) btn.classList.add("disabled");
            }

            btn.onclick = async () => {
                try {
                    // 시작 전: 아무 타일 클릭 -> start
                    if (!state.started) {
                        state = await apiStart(x, y);
                        render();
                        return;
                    }

                    // 시작 후: 인접만 move
                    if (!isAdjacent(state.posX, state.posY, x, y)) {
                        toast("인접 타일만 이동 가능");
                        return;
                    }

                    if (state.ap <= 0) {
                        toast("AP 부족");
                        return;
                    }

                    state = await apiMove(x, y);
                    render();
                } catch (e) {
                    toast(e.message);
                }
            };

            gridEl.appendChild(btn);
        }
    }
}

async function boot() {
    try {
        state = await apiGetState();
        render();
    } catch (e) {
        toast(e.message);
        statusEl.textContent = "초기 로딩 실패";
    }
}

btnReload.onclick = boot;

boot();
