// === DOM 요소 ===
const chatWindow = document.getElementById("chat-window");
const chatInput = document.getElementById("chat-input");
const sendBtn = document.getElementById("chat-send-btn");
const statusEl = document.getElementById("chat-status");

// 파티 영역
const partyArea = document.querySelector(".party-area");

function renderParty(party) {
    if (!partyArea) return;
    partyArea.innerHTML = "";

    const title = document.createElement("div");
    title.className = "section-title";
    title.textContent = "Party";
    partyArea.appendChild(title);

    party.forEach(member => {
        const row = document.createElement("div");
        row.className = "character-row";

        const nameDiv = document.createElement("div");
        nameDiv.className = "character-name";
        nameDiv.textContent = member.name;

        const hpWrapper = document.createElement("div");
        hpWrapper.className = "char-hp-wrapper";

        const hpFill = document.createElement("div");
        hpFill.className = "char-hp-fill";
        hpFill.style.width = (member.hpRatio ?? 0) + "%";

        hpWrapper.appendChild(hpFill);

        const hpText = document.createElement("div");
        hpText.className = "char-hp-text";
        hpText.textContent = member.hp + " / " + member.maxHp;

        row.appendChild(nameDiv);
        row.appendChild(hpWrapper);
        row.appendChild(hpText);

        partyArea.appendChild(row);
    });
}

// 템플릿에서 data-* 로 내려준 값
const username =
    (chatWindow && chatWindow.dataset.username) ||
    ("guest" + Math.floor(Math.random() * 1000));

let roomId =
    (chatWindow && chatWindow.dataset.roomId) ||
    "raid-1";

let socket = null;

function addMessage(text, cssClass) {
    const div = document.createElement("div");
    if (cssClass) div.className = "chat-message " + cssClass;
    div.textContent = text;
    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

function setStatus(text) {
    if (statusEl) statusEl.textContent = text;
}

// 보스 HP 갱신
function updateBossHp(current, max) {
    const bar = document.getElementById("boss-hp-bar");
    const text = document.getElementById("boss-hp-text");
    if (!bar || !text) return;

    const ratio = Math.max(0, Math.min(100, (current / max) * 100));
    bar.style.width = ratio + "%";
    text.textContent = `HP ${current} / ${max} (${Math.round(ratio)}%)`;
}

function connect() {
    console.log("웹소켓 연결 시도");

    const wsProtocol = window.location.protocol === "https:" ? "wss://" : "ws://";
    const host = window.location.host;
    const wsUrl = wsProtocol + host + "/ws/chat";
    console.log("Connecting to:", wsUrl);

    socket = new WebSocket(wsUrl);

    socket.onopen = () => {
        console.log("onopen");
        setStatus("✅ 서버와 연결되었습니다. (방: " + roomId + ")");
        addMessage("시스템: " + roomId + " 방에 입장합니다.", "system");

        const joinMsg = {
            type: "JOIN",
            sender: username,
            roomId: roomId,
            message: ""
        };
        socket.send(JSON.stringify(joinMsg));
    };

    socket.onmessage = (event) => {
        console.log("onmessage raw:", event.data);
        let data;
        try {
            data = JSON.parse(event.data);
        } catch (e) {
            console.error("JSON 파싱 실패:", event.data);
            addMessage(event.data, "other");
            return;
        }

        let text = "";
        let cssClass = "other";

        switch (data.type) {
            case "SYSTEM":
                text = "시스템: " + (data.message || "");
                cssClass = "system";
                break;

            case "CHAT":
                if (data.sender === username) {
                    text = "나: " + (data.message || "");
                    cssClass = "me";
                } else {
                    text = (data.sender || "알 수 없음") + ": " + (data.message || "");
                    cssClass = "other";
                }
                break;

            case "ATTACK_RESULT":
                text = "[공격] " + (data.message || "");
                if (data.bossHp != null && data.maxHp != null) {
                    text += " (보스 HP: " + data.bossHp + " / " + data.maxHp + ")";
                    updateBossHp(data.bossHp, data.maxHp);
                }
                cssClass = "system";
                break;

            case "PARTY_UPDATE":
                console.log("PARTY_UPDATE 수신:", data.party);
                if (Array.isArray(data.party)) {
                    renderParty(data.party);
                }
                return;

            default:
                text = "[" + data.type + "] " +
                    (data.sender || "") + " " +
                    (data.message || "");
                cssClass = "other";
        }

        addMessage(text, cssClass);
    };

    socket.onclose = (event) => {
        console.log("onclose:", event);
        setStatus("❌ 연결이 종료되었습니다. (3초 후 재접속)");
        addMessage("시스템: 연결이 종료되었습니다.", "system");
        setTimeout(connect, 3000);
    };

    socket.onerror = (error) => {
        console.log("onerror:", error);
        setStatus("⚠ 연결 오류가 발생했습니다.");
        addMessage("시스템: 연결 오류가 발생했습니다.", "system");
    };
}

function sendMessage() {
    const text = chatInput.value.trim();
    if (!text || !socket || socket.readyState !== WebSocket.OPEN) {
        return;
    }

    if (text.startsWith("/atk")) {
        const parts = text.split(" ");
        let damage = null;
        if (parts.length > 1) {
            const parsed = parseInt(parts[1], 10);
            if (!isNaN(parsed)) damage = parsed;
        }

        const attackMsg = {
            type: "ATTACK",
            sender: username,
            roomId: roomId,
            damage: damage
        };
        socket.send(JSON.stringify(attackMsg));
    } else {
        const msg = {
            type: "CHAT",
            sender: username,
            roomId: roomId,
            message: text
        };
        socket.send(JSON.stringify(msg));
    }

    chatInput.value = "";
    chatInput.focus();
}

if (sendBtn && chatInput) {
    sendBtn.addEventListener("click", sendMessage);
    chatInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") sendMessage();
    });
}

connect();
