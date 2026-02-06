const grid = document.getElementById("grid");
const mapId = Number(grid.dataset.mapId);
const w = Number(grid.dataset.w);
const h = Number(grid.dataset.h);

let selected = {x:null, y:null};

function toast(msg, ok=true){
    const t = document.getElementById("toast");
    t.style.display = "block";
    t.textContent = msg;
    t.className = "toast " + (ok ? "ok" : "bad");
    setTimeout(()=>t.style.display="none", 1400);
}

function cellSelector(x,y){ return `.cell[data-x="${x}"][data-y="${y}"]`; }

function setSelected(btn){
    document.querySelectorAll(".cell.selected").forEach(e=>e.classList.remove("selected"));
    btn.classList.add("selected");
}

async function loadPlacements(){
    const res = await fetch(`/admin/explore/api/placements?mapId=${mapId}`);
    const arr = await res.json();
    arr.forEach(p=>{
        const btn = document.querySelector(cellSelector(p.x,p.y));
        if(btn) btn.classList.add("has-event");
    });
}

async function loadTile(x,y){
    const res = await fetch(`/admin/explore/api/tile?mapId=${mapId}&x=${x}&y=${y}`);
    return await res.json();
}

function renderPreview(preview){
    const wrap = document.getElementById("preview");
    if(!preview){
        wrap.className = "preview muted";
        wrap.innerHTML = "이벤트 없음";
        return;
    }
    wrap.className = "preview";
    wrap.innerHTML = `
    <div class="card">
      <div class="row">
        <div class="thumb">${preview.backgroundUrl ? `<img src="${preview.backgroundUrl}">` : `<div class="ph">BG</div>`}</div>
        <div>
          <div class="big"><b>${preview.code}</b> · ${preview.name} <span class="pill">type=${preview.eventType}</span></div>
          <div class="muted">${preview.npcName ?? ""}</div>
        </div>
      </div>
      <div class="row vn">
        ${preview.npcPortraitUrl ? `<img class="npc" src="${preview.npcPortraitUrl}">` : ``}
        <pre class="script">${preview.scriptPreview ?? ""}</pre>
      </div>
    </div>
  `;
}

async function saveTile(){
    if(selected.x === null) return toast("타일 선택부터!", false);

    const blocked = document.getElementById("blocked").checked;
    const eventIdVal = document.getElementById("eventSelect").value;
    const eventTypeVal = document.getElementById("eventType").value;

    const body = {
        mapId,
        x: selected.x,
        y: selected.y,
        blocked,
        eventId: eventIdVal ? Number(eventIdVal) : null,
        eventType: eventTypeVal ? Number(eventTypeVal) : null
    };

    const res = await fetch(`/admin/explore/api/tile`, {
        method:"POST",
        headers:{ "Content-Type":"application/json" },
        body: JSON.stringify(body)
    });

    const r = await res.json();
    toast(r.message, r.ok);

    const btn = document.querySelector(cellSelector(selected.x, selected.y));
    if(btn){
        btn.classList.toggle("blocked", blocked);
        btn.classList.toggle("has-event", !!body.eventId);
    }

    const data = await loadTile(selected.x, selected.y);
    renderPanel(data);
}

async function clearEvent(){
    document.getElementById("eventSelect").value = "";
    document.getElementById("eventType").value = "";
    await saveTile();
}

function renderPanel(data){
    document.getElementById("xy").textContent = `${data.x}, ${data.y}`;
    document.getElementById("blockedText").textContent = data.blocked ? "YES" : "NO";
    document.getElementById("eventText").textContent = data.hasEvent ? `id=${data.eventId} type=${data.eventType}` : "-";

    document.getElementById("blocked").checked = !!data.blocked;
    document.getElementById("eventSelect").value = data.eventId ?? "";
    document.getElementById("eventType").value = data.eventType ?? "";

    renderPreview(data.eventPreview);
}

function initGrid(){
    grid.style.gridTemplateColumns = `repeat(${w}, 1fr)`;

    for(let y=0;y<h;y++){
        for(let x=0;x<w;x++){
            const b = document.createElement("button");
            b.className = "cell";
            b.dataset.x = x;
            b.dataset.y = y;
            b.title = `${x},${y}`;

            b.addEventListener("click", async ()=>{
                selected = {x,y};
                setSelected(b);
                const data = await loadTile(x,y);
                renderPanel(data);
            });

            grid.appendChild(b);
        }
    }
}

document.getElementById("saveBtn").addEventListener("click", saveTile);
document.getElementById("clearBtn").addEventListener("click", clearEvent);

initGrid();
loadPlacements();
