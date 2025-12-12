// === DOM 요소 ===
const chatWindow = document.getElementById("chat-window");
const chatInput = document.getElementById("chat-input");
const sendBtn = document.getElementById("chat-send-btn");
const statusEl = document.getElementById("chat-status");

// ADMIN 전투 시작 버튼 (관리자에게만 렌더됨)
const adminStartBtn = document.getElementById("admin-start-btn");

// 🔥 엔딩 후 로비로 돌아가기 버튼
const backBtn = document.getElementById("back-to-lobby-btn");

// 🗡 공격 모드 라디오 / 방어 대상 셀렉트
const defendSelect = document.getElementById("defend-target-select");

// ===== 전역 상태 =====
let currentTurn = null;      // 지금 몇 턴인지
let gameOver = false;        // 엔딩 여부

// 🔥 보스 공격 로그 버퍼 (한 턴 단위로 모으기)
let bossAttackBuffer = [];
let bossAttackTurn = null;

// 엔딩 연출 대사
const victoryScript = [
    "용의 거대한 몸이 흔들리며 균열이 간다.",
    "마지막 비명이 방 안을 울린다.",
    "적막이 내려앉는다. 전투는 끝났다."
];

const defeatScript = [
    "모든 빛이 서서히 사라져간다.",
    "몸이 움직이지 않는다. 숨소리마저 멀어진다.",
    "이번 싸움은 여기서 끝났다."
];

// 파티 영역
const partyArea = document.querySelector(".party-area");

// 템플릿에서 data-* 로 내려준 값
const username =
    (chatWindow && chatWindow.dataset.username) ||
    ("guest" + Math.floor(Math.random() * 1000));

let roomId =
    (chatWindow && chatWindow.dataset.roomId) ||
    "raid-1";

let socket = null;

// ================== 공통 렌더링 유틸 ==================

function appendToChat(node) {
    if (!chatWindow) return;
    chatWindow.appendChild(node);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

// 시스템 메시지
function renderSystemMessage(text) {
    const div = document.createElement("div");
    div.className = "chat-message msg-system";
    div.textContent = text;
    appendToChat(div);
}

// 턴 메시지
function renderTurnMessage(turnTextOrNumber) {
    const div = document.createElement("div");
    div.className = "chat-message msg-turn";

    if (typeof turnTextOrNumber === "number") {
        div.textContent = `${turnTextOrNumber}`;
    } else {
        div.textContent = turnTextOrNumber;
    }
    appendToChat(div);
}

// 유저 채팅
function renderChatMessage(sender, text) {
    const div = document.createElement("div");
    const isMe = (sender === username);

    div.className = "chat-message msg-chat " + (isMe ? "msg-chat-me" : "msg-chat-other");
    div.textContent = (isMe ? "나" : (sender || "알 수 없음")) + ": " + text;

    appendToChat(div);
}

// 일반 텍스트(공격 코멘트, 엔딩 대사 등)
function renderPlainMessage(text, cssClass) {
    const div = document.createElement("div");
    div.className = "chat-message" + (cssClass ? (" " + cssClass) : "");
    div.textContent = text;
    appendToChat(div);
}

// 기존 addMessage는 그냥 wrapper
function addMessage(text, cssClass) {
    renderPlainMessage(text, cssClass);
}

function flushBossAttackBox() {
    if (!bossAttackBuffer.length) return;

    // 턴 정보 (없으면 "이번 턴" 정도로 처리)
    const turnLabel = bossAttackTurn != null
        ? `[TURN ${bossAttackTurn}] `
        : "";

    const wrapper = document.createElement("div");
    wrapper.className = "chat-message boss-attack-group";

    // 상단 제목
    const header = document.createElement("div");
    header.className = "boss-attack-group-title";
    header.textContent = `${turnLabel} RESULT`;
    wrapper.appendChild(header);

    // 각 라인
    bossAttackBuffer.forEach(hit => {
        const lineDiv = document.createElement("div");
        lineDiv.className = "boss-attack-line";

        let line = `${hit.name}에게 ${hit.dmg} 피해`;
        if (hit.hp != null && hit.maxHp != null) {
            line += ` (HP ${hit.hp} / ${hit.maxHp})`;
        }
        if (hit.defense != null) {
            line += `, 방어 ${hit.defense}`;
        }

        lineDiv.textContent = line;
        wrapper.appendChild(lineDiv);
    });

    appendToChat(wrapper);

    // 버퍼 초기화
    bossAttackBuffer = [];
    bossAttackTurn = null;
}


// send 버튼 활성/비활성
function setSendEnabled(enabled) {
    if (sendBtn) sendBtn.disabled = !enabled;
}

// 엔딩 연출 대사
function playEndScript(lines, callback) {
    let acc = 0;
    lines.forEach(line => {
        acc += 3000;
        setTimeout(() => {
            renderPlainMessage(line, "ending-text");
        }, acc);
    });
    if (callback) {
        setTimeout(callback, acc + 500);
    }
}

function enableBackToLobby() {
    if (backBtn) {
        backBtn.disabled = false;
        backBtn.classList.add("active");
    }
}

// ================== UI: 보스 HP / 스킬 / 파티 ==================

function updateBossSkillSlot(index, cdNow, cdMax, available, name, desc) {
    const slot = document.querySelector(
        `.boss-skill-slot[data-skill-index="${index}"]`
    );
    if (!slot) return;

    const numSpan = slot.querySelector(".cooldown-number");
    const tooltip = slot.querySelector(".boss-skill-tooltip");

    // 🔹 쿨다운 숫자 표시
    if (numSpan) {
        if (cdNow >= 2) {
            numSpan.textContent = cdNow;
            numSpan.style.display = "block";
        } else {
            numSpan.textContent = "";
            numSpan.style.display = "none";
        }
    }

    // 🔹 ready 상태 표시
    if (available || cdNow === 1) {
        slot.classList.add("ready");
    } else {
        slot.classList.remove("ready");
    }

    // 🔹 툴팁 텍스트 (이 부분이 중요!)
    if (tooltip) {
        const nameEl =
            tooltip.querySelector(".boss-skill-name") ||
            tooltip.querySelector("strong");

        const descEl =
            tooltip.querySelector(".boss-skill-desc") ||
            tooltip.querySelector("p") ||
            tooltip.querySelector("div");

        if (nameEl) {
            nameEl.textContent = name || "";
        }
        if (descEl) {
            descEl.textContent = desc || "";
        }
    }
}


function renderParty(party) {
    if (!partyArea) return;
    partyArea.innerHTML = "";

    // 제목
    const title = document.createElement("div");
    title.className = "section-title";
    title.textContent = "Party";
    partyArea.appendChild(title);

    party.forEach(member => {
        const row = document.createElement("div");
        row.className = "character-row";

        const card = document.createElement("div");
        card.className = "char-card";

        // 🔹 두상
        const portrait = document.createElement("div");
        portrait.className = "char-portrait";
        if (member.avatarUrl) {
            portrait.style.backgroundImage = `url('${member.avatarUrl}')`;
            portrait.style.backgroundSize = "cover";
            portrait.style.backgroundPosition = "center";
        }

        const info = document.createElement("div");
        info.className = "char-info";

        // === 1. 이름 + AP ===
        const header = document.createElement("div");
        header.className = "char-header";

        const nameSpan = document.createElement("span");
        nameSpan.className = "char-name";
        nameSpan.textContent = member.name;

        const apSpan = document.createElement("span");
        apSpan.className = "char-ap-text";
        const apValue =
            member.ap ??
            member.actionPoint ??
            member.apNow ??
            0;
        apSpan.textContent = `AP ${apValue}`;

        header.appendChild(nameSpan);
        header.appendChild(apSpan);

        // === 2. 스탯 4개 (ATK / INT / DET / HP) ===
        const stats = document.createElement("div");
        stats.className = "char-stats";

        const row1 = document.createElement("div");
        row1.className = "char-stat-row";

        const atkLabel = document.createElement("span");
        atkLabel.className = "label";
        atkLabel.textContent = "ATK";
        const atkVal = document.createElement("span");
        atkVal.className = "value";
        const atkValue =
            member.atkStat ??
            member.atk ??
            member.attack ??
            "-";
        atkVal.textContent = atkValue;

        const intLabel = document.createElement("span");
        intLabel.className = "label";
        intLabel.textContent = "INT";
        const intVal = document.createElement("span");
        intVal.className = "value";
        const intValue =
            member.intStat ??
            member.intelligence ??
            "-";
        intVal.textContent = intValue;

        row1.appendChild(atkLabel);
        row1.appendChild(atkVal);
        row1.appendChild(intLabel);
        row1.appendChild(intVal);

        const row2 = document.createElement("div");
        row2.className = "char-stat-row";

        const detLabel = document.createElement("span");
        detLabel.className = "label";
        detLabel.textContent = "DET";
        const detVal = document.createElement("span");
        detVal.className = "value";
        const detValue =
            member.detStat ??
            member.det ??
            "-";
        detVal.textContent = detValue;

        const hpLabel = document.createElement("span");
        hpLabel.className = "label";
        hpLabel.textContent = "HP";
        const hpVal = document.createElement("span");
        hpVal.className = "value";
        const hpStatValue =
            member.hpStat ??
            member.hpBase ??
            member.hp ??
            "-";
        hpVal.textContent = hpStatValue;

        row2.appendChild(detLabel);
        row2.appendChild(detVal);
        row2.appendChild(hpLabel);
        row2.appendChild(hpVal);

        stats.appendChild(row1);
        stats.appendChild(row2);

        // === 3. 스킬 2개 (AP 아래, 스탯 오른쪽 세로) ===
        // ✅ 여기 추가: 스킬 배열 만들기 (skill1/skill2)
        const skills = [member.skill1 ?? null, member.skill2 ?? null];

        const skillCol = document.createElement("div");
        skillCol.className = "char-skill-col";

        [1, 2].forEach((idx) => {
            const slot = document.createElement("div");
            slot.className = "char-skill-slot";
            slot.dataset.skillIndex = String(idx);

            const icon = document.createElement("div");
            icon.className = "char-skill-icon" + (idx === 2 ? " skill-2" : "");

            const tooltip = document.createElement("div");
            tooltip.className = "char-skill-tooltip";

            const strong = document.createElement("strong");
            const p = document.createElement("p");

            const s = skills[idx - 1];

            if (s) {
                if (s.imageUrl) {
                    icon.style.backgroundImage = `url('${s.imageUrl}')`;
                    icon.style.backgroundSize = "cover";
                    icon.style.backgroundPosition = "center";
                }
                strong.textContent = s.name ?? `스킬 ${idx}`;
                p.textContent = s.effectText?.trim()
                    ? s.effectText
                    : (s.description ?? "");
            } else {
                strong.textContent = `스킬 ${idx}`;
                p.textContent = "미장착";
                slot.classList.add("empty");
            }

            tooltip.appendChild(strong);
            tooltip.appendChild(p);
            slot.appendChild(icon);
            slot.appendChild(tooltip);
            skillCol.appendChild(slot);
        });

        // === 4. AP 아래 한 줄: 왼쪽 stats / 오른쪽 skillCol ===
        const bodyRow = document.createElement("div");
        bodyRow.className = "char-body-row";
        bodyRow.appendChild(stats);
        bodyRow.appendChild(skillCol);

        // === 5. HP 바 ===
        const hpWrapper = document.createElement("div");
        hpWrapper.className = "char-hp-wrapper";

        const hpFill = document.createElement("div");
        hpFill.className = "char-hp-fill";

        const ratio =
            member.hpRatio ??
            ((member.hp != null && member.maxHp > 0)
                ? Math.round((member.hp / member.maxHp) * 100)
                : 0);

        hpFill.style.width = ratio + "%";
        hpWrapper.appendChild(hpFill);

        const hpText = document.createElement("div");
        hpText.className = "char-hp-text";
        hpText.textContent = `${member.hp} / ${member.maxHp}`;

        // === 조립 ===
        info.appendChild(header);
        info.appendChild(bodyRow);
        info.appendChild(hpWrapper);
        info.appendChild(hpText);

        card.appendChild(portrait);
        card.appendChild(info);
        row.appendChild(card);

        partyArea.appendChild(row);
    });
}



function updateBossHp(current, max) {
    const bar = document.getElementById("boss-hp-bar");
    const text = document.getElementById("boss-hp-text");
    if (!bar || !text) return;

    const ratio = Math.max(0, Math.min(100, (current / max) * 100));
    bar.style.width = ratio + "%";
    text.textContent = `HP ${current} / ${max} (${Math.round(ratio)}%)`;
}

function updateBossAp(current, max) {
    const text = document.getElementById("boss-ap-text");
    if (!text) return;

    if (max != null && !Number.isNaN(max)) {
        text.textContent = `AP ${current} / ${max}`;
    } else {
        text.textContent = `AP ${current}`;
    }
}

// ================== 액션 모드 / 라디오 ==================

function getActionMode() {
    const checked = document.querySelector('input[name="action-mode"]:checked');
    return checked ? checked.value : "CHAT";
}

document.querySelectorAll('input[name="action-mode"]').forEach(radio => {
    radio.addEventListener("change", () => {
        const mode = getActionMode();
        if (defendSelect) {
            defendSelect.disabled = (mode !== "DEFEND");
        }
    });
});

// ================== WebSocket ==================

function setStatus(text) {
    if (statusEl) statusEl.textContent = text;
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
        renderSystemMessage(roomId + " 방에 입장합니다.");

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
            renderPlainMessage(event.data, "other");
            return;
        }

        switch (data.type) {
            case "SYSTEM": {
                renderSystemMessage(data.message || "");
                return;
            }

            // TURN_START: 전투 시작 / 다음 턴 시작
            case "TURN_START": {
                // 🔥 먼저 직전 턴의 보스 공격 묶음을 출력
                flushBossAttackBox();

                const tNum = Number(data.turn);
                currentTurn = tNum;

                // 🔥 0.3초 정도 여유를 줘서 이전 메시지가 완료된 느낌 만들기
                setTimeout(() => {
                    renderTurnMessage(tNum);

                    if (data.message) {
                        renderSystemMessage(data.message);
                    }

                    updateBossSkillSlot(1, data.skill1CdNow, data.skill1CdMax, data.skill1Available, data.skill1Name, data.skill1Desc);
                    updateBossSkillSlot(2, data.skill2CdNow, data.skill2CdMax, data.skill2Available, data.skill2Name, data.skill2Desc);
                    updateBossSkillSlot(3, data.skill3CdNow, data.skill3CdMax, data.skill3Available, data.skill3Name, data.skill3Desc);
                }, 1000);

                return;
            }


            case "CHAT": {
                renderChatMessage(data.sender, data.message || "");
                return;
            }

            case "ATTACK_RESULT": {
                const comment = (data.comment || "").trim();

                if (comment) {
                    const isMe = data.sender === username;
                    const whoLabel = isMe
                        ? "나(공격)"
                        : ((data.sender || "알 수 없음") + "(공격)");

                    renderPlainMessage(
                        `${whoLabel}: ${comment}`,
                        isMe ? "msg-attack-me" : "msg-attack-other"
                    );
                }

                let systemText = `[공격] ` + (data.message || "");
                if (data.bossHp != null && data.maxHp != null) {
                    systemText += " (보스 HP: " + data.bossHp + " / " + data.maxHp + ")";
                    updateBossHp(data.bossHp, data.maxHp);
                }
                renderSystemMessage(systemText);
                return;
            }


            case "DEFEND_RESULT": {
                const comment = (data.comment || "").trim();

                if (comment) {
                    const isMe = data.sender === username;
                    const whoLabel = isMe
                        ? "나(방어)"
                        : ((data.sender || "알 수 없음") + "(방어)");

                    renderPlainMessage(
                        `${whoLabel}: ${comment}`,
                        isMe ? "msg-defend-me" : "msg-defend-other"
                    );
                }

                let systemText = "[방어] " + (data.message || "");
                renderSystemMessage(systemText);
                return;
            }


            case "PARTY_UPDATE": {
                // 🔥 먼저 직전 턴의 보스 공격 묶음을 출력
                flushBossAttackBox(); //얘 ㄴㅁ 핫픽스다....

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
            }


            case "BOSS_ATTACK": {
                const name = data.targetName || "알 수 없는 대상";
                const dmg = (data.damage != null) ? data.damage : 0;
                const defense = (data.defense != null) ? data.defense : null;

                // 턴 번호가 바뀌면 이전 턴 버퍼를 먼저 플러시
                const t = (data.turn != null) ? Number(data.turn) : null;
                if (bossAttackTurn != null && t != null && t !== bossAttackTurn) {
                    flushBossAttackBox();
                }
                if (bossAttackTurn == null && t != null) {
                    bossAttackTurn = t;
                }

                // 일단 버퍼에 쌓기만 한다
                bossAttackBuffer.push({
                    name,
                    dmg,
                    hp: (data.targetHp != null ? data.targetHp : null),
                    maxHp: (data.targetMaxHp != null ? data.targetMaxHp : null),
                    defense
                });

                // 🔥 여기서는 바로 출력하지 않는다!
                return;
            }

            case "BOSS_DEAD": {
                // 🔥 먼저 직전 턴의 보스 공격 묶음을 출력
                flushBossAttackBox();

                gameOver = true;

                const msg = data.message
                    ? "[보스 처치] " + data.message
                    : "[보스 처치] 전투에서 승리했습니다.";

                renderSystemMessage(msg);

                setTimeout(() => {
                    playEndScript(victoryScript, () => {
                        enableBackToLobby();
                    });
                }, 300);
                return;
            }

            case "GAME_OVER": {
                // 🔥 먼저 직전 턴의 보스 공격 묶음을 출력
                flushBossAttackBox();

                gameOver = true;

                const msg = data.message
                    ? "[전투 패배] " + data.message
                    : "[전투 패배] 파티가 전멸했습니다.";

                renderSystemMessage(msg);

                setTimeout(() => {
                    playEndScript(defeatScript, () => {
                        enableBackToLobby();
                    });
                }, 300);
                return;
            }

            default: {
                const text = "[" + data.type + "] " +
                    (data.sender || "") + " " +
                    (data.message || "");
                renderPlainMessage(text, "other");
                return;
            }
        }
    };

    socket.onclose = (event) => {
        console.log("onclose:", event);
        setStatus("❌ 연결이 종료되었습니다. (3초 후 재접속)");
        renderSystemMessage("시스템: 연결이 종료되었습니다.");
        setTimeout(connect, 3000);
    };

    socket.onerror = (error) => {
        console.log("onerror:", error);
        setStatus("⚠ 연결 오류가 발생했습니다.");
        renderSystemMessage("시스템: 연결 오류가 발생했습니다.");
    };
}

// ================== ADMIN / SEND ==================

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

    if (gameOver && mode !== "CHAT") {
        return;
    }

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

// ================== 입장 연출 오버레이 ==================

document.addEventListener("DOMContentLoaded", () => {
    const overlay = document.getElementById("raid-entry-overlay");
    if (!overlay) {
        connect();
        return;
    }

    const fadeDuration = 800;
    const delayBeforeFade = 200;

    setTimeout(() => {
        overlay.classList.remove("show");
        overlay.classList.add("fade-out");
    }, delayBeforeFade);

    setTimeout(() => {
        if (overlay && overlay.parentNode) {
            overlay.parentNode.removeChild(overlay);
        }
    }, delayBeforeFade + fadeDuration + 50);

    connect();
});
