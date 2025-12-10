// === DOM 요소 ===
const chatWindow = document.getElementById("chat-window");
const chatInput = document.getElementById("chat-input");
const sendBtn = document.getElementById("chat-send-btn");
const statusEl = document.getElementById("chat-status");

// ADMIN 전투 시작 버튼 (관리자에게만 렌더됨)
const adminStartBtn = document.getElementById("admin-start-btn");

// 🗡 공격 모드 체크박스 (지금은 안 쓰지만 남겨둠)
const attackCheckbox = document.getElementById("attack-mode-checkbox");
const defendSelect = document.getElementById("defend-target-select");

let currentTurn = null;
let delayedBase = 0;   // 🔥 보스 공격/턴 시작 딜레이 누적용

// 파티 영역
const partyArea = document.querySelector(".party-area");

function queueSystemMessage(text, cssClass) {
    if (!text) return;
    delayedBase += 3000; // 3초씩 밀기
    const delay = delayedBase;
    setTimeout(() => {
        addMessage(text, cssClass);
    }, delay);
}

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

function getActionMode() {
    const checked = document.querySelector('input[name="action-mode"]:checked');
    return checked ? checked.value : "CHAT";
}

// 🔥 턴 헤더 찍기
function ensureTurnHeader(turn) {
    if (turn == null) return;
    if (currentTurn === turn) return;

    currentTurn = turn;
    addMessage(`[${turn}턴]`, "turn-header");
}

// 라디오 변경 시 DEFEND이면 셀렉트 활성화, 아니면 비활성화
document.querySelectorAll('input[name="action-mode"]').forEach(radio => {
    radio.addEventListener("change", () => {
        const mode = getActionMode();
        if (defendSelect) {
            defendSelect.disabled = (mode !== "DEFEND");
        }
    });
});

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

            // 🔥 TURN_START: admin이 전투 시작 눌렀을 때
            case "TURN_START": {
                const t = data.turn;

                if (t === 1) {
                    // 🔥 최초 1턴은 바로 출력
                    currentTurn = t;
                    addMessage(`[${t}턴]`, "turn-header");
                    if (data.message) {
                        addMessage("시스템: " + data.message, "system");
                    }
                } else {
                    // 🔥 그 이후 턴은 보스 공격 다음에 3초 딜레이로 출력
                    queueSystemMessage(`[${t}턴]`, "turn-header");
                    if (data.message) {
                        queueSystemMessage("시스템: " + data.message, "system");
                    }
                }
                return;
            }

            case "CHAT":
                if (data.sender === username) {
                    text = "나: " + (data.message || "");
                    cssClass = "me";
                } else {
                    text = (data.sender || "알 수 없음") + ": " + (data.message || "");
                    cssClass = "other";
                }
                break;

            case "ATTACK_RESULT": {
                // 🔥 이번 턴의 로그 시작이므로 딜레이 초기화
                delayedBase = 0;

                if (data.comment) {
                    const who = data.sender === username ? "나(공격)" : (data.sender || "알 수 없음");
                    addMessage(who + ": " + data.comment, "attack-text");
                }

                let systemText = "[공격] " + (data.message || "");
                if (data.bossHp != null && data.maxHp != null) {
                    systemText += " (보스 HP: " + data.bossHp + " / " + data.maxHp + ")";
                    updateBossHp(data.bossHp, data.maxHp);
                }
                addMessage(systemText, "system"); // 바로 출력

                return;
            }

            case "DEFEND_RESULT": {
                delayedBase = 0;

                if (data.comment) {
                    const who = data.sender === username ? "나(방어)" : (data.sender || "알 수 없음");
                    addMessage(who + ": " + data.comment, "defend-text");
                }
                let systemText = "[방어] " + (data.message || "");
                addMessage(systemText, "system");

                return;
            }

            case "PARTY_UPDATE":
                console.log("PARTY_UPDATE 수신:", data.party);
                if (Array.isArray(data.party)) {
                    renderParty(data.party);

                    if (defendSelect) {
                        defendSelect.innerHTML = '<option value="">방어 대상 선택</option>';

                        data.party.forEach(member => {
                            const opt = document.createElement("option");
                            opt.value = member.characterId;
                            opt.textContent = member.name;
                            defendSelect.appendChild(opt);
                        });
                    }
                }
                return;

            case "BOSS_ATTACK": {
                const name = data.targetName || "알 수 없는 대상";
                const dmg = (data.damage != null) ? data.damage : 0;
                const defense = (data.defense != null) ? data.defense : null;

                let line = `[보스 공격] ${name}에게 ${dmg} 피해`;
                if (data.targetHp != null && data.targetMaxHp != null) {
                    line += ` (HP ${data.targetHp} / ${data.targetMaxHp})`;
                }
                if (defense !== null) {
                    line += `, 방어 ${defense}`;
                }

                // 🔥 3초씩 밀리면서 순차 출력
                queueSystemMessage(line, "system");
                return;
            }

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

// 🔥 ADMIN용 전투 시작 버튼 → ADMIN 메시지 전송
if (adminStartBtn) {
    adminStartBtn.addEventListener("click", () => {
        if (!socket || socket.readyState !== WebSocket.OPEN) return;

        const adminMsg = {
            type: "ADMIN",
            sender: username,
            roomId: roomId,
            command: "START_BATTLE"
        };
        socket.send(JSON.stringify(adminMsg));
    });
}

function sendMessage() {
    if (!socket || socket.readyState !== WebSocket.OPEN) return;

    const text = chatInput.value.trim();
    const mode = getActionMode();

    if (mode === "CHAT" && !text) {
        return;
    }

    if (mode === "ATTACK") {
        const attackMsg = {
            type: "ATTACK",
            sender: username,
            roomId: roomId,
            comment: text || null
        };
        socket.send(JSON.stringify(attackMsg));

        chatInput.value = "";
        chatInput.focus();
        return;
    }

    if (mode === "DEFEND") {
        const targetIdStr = defendSelect && defendSelect.value ? defendSelect.value : null;
        if (!targetIdStr) {
            return;
        }

        const defendMsg = {
            type: "DEFEND",
            sender: username,
            roomId: roomId,
            targetCharacterId: Number(targetIdStr),
            comment: text || null
        };
        socket.send(JSON.stringify(defendMsg));

        chatInput.value = "";
        chatInput.focus();
        return;
    }

    const msg = {
        type: "CHAT",
        sender: username,
        roomId: roomId,
        message: text
    };
    socket.send(JSON.stringify(msg));

    chatInput.value = "";
    chatInput.focus();
}

if (sendBtn && chatInput) {
    sendBtn.addEventListener("click", sendMessage);
    chatInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            sendMessage();
        }
    });
}

connect();
