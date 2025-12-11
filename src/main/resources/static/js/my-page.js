const chatWindow   = document.getElementById("chat-window");
const chatInput    = document.getElementById("chat-input");
const sendBtn      = document.getElementById("chat-send-btn");
const statusEl     = document.getElementById("chat-status");
const enterRaidBtn = document.getElementById("enter-raid-btn");

// 입장 연출 오버레이 요소
const raidOverlay = document.getElementById("raid-entry-overlay");
const raidTitleEl = document.getElementById("raid-entry-title");
const raidDescEl  = document.getElementById("raid-entry-desc");

// 템플릿에서 내려준 값
const username =
    (chatWindow && chatWindow.dataset.username) ||
    ("guest" + Math.floor(Math.random() * 1000));

let roomId =
    (chatWindow && chatWindow.dataset.roomId) ||
    "lobby-1";

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

function connect() {
    console.log("웹소켓(로비) 연결 시도");

    const wsProtocol = window.location.protocol === "https:" ? "wss://" : "ws://";
    const host = window.location.host;
    const wsUrl = wsProtocol + host + "/ws/chat";
    console.log("Connecting to:", wsUrl);

    socket = new WebSocket(wsUrl);

    socket.onopen = () => {
        setStatus("✅ 로비 채팅에 연결되었습니다. (방: " + roomId + ")");
        addMessage("시스템: 로비(" + roomId + ") 에 입장합니다.", "system");

        const joinMsg = {
            type: "JOIN",
            sender: username,
            roomId: roomId,
            message: ""
        };
        socket.send(JSON.stringify(joinMsg));
    };

    socket.onmessage = (event) => {
        let data;
        try {
            data = JSON.parse(event.data);
        } catch (e) {
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
            default:
                text = "[" + data.type + "] " +
                    (data.sender || "") + " " +
                    (data.message || "");
                cssClass = "other";
        }

        addMessage(text, cssClass);
    };

    socket.onclose = () => {
        console.log("❌ 웹소켓 연결 끊김 — 3초 후 재접속 시도");
        setTimeout(connect, 3000);
    };
}

function sendMessage() {
    const text = chatInput.value.trim();
    if (!text || !socket || socket.readyState !== WebSocket.OPEN) {
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

// 이벤트 바인딩
if (sendBtn && chatInput) {
    sendBtn.addEventListener("click", sendMessage);
    chatInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") sendMessage();
    });
}
// ✅ 여기서 입장 연출 처리
if (enterRaidBtn && raidOverlay && raidTitleEl && raidDescEl) {
    const raidUrl  = enterRaidBtn.dataset.raidUrl;
    const raidName = enterRaidBtn.dataset.raidName || "레이드";
    const raidDesc = enterRaidBtn.dataset.raidDesc || "";

    let isTransitioning = false;

    enterRaidBtn.addEventListener("click", (e) => {
        e.preventDefault();
        if (isTransitioning) return;
        isTransitioning = true;

        console.log("입장 연출 시작:", raidUrl, raidName, raidDesc);

        raidTitleEl.textContent = raidName;
        raidDescEl.textContent  = raidDesc;

        // 혹시 남아있을지도 모를 상태 초기화
        raidOverlay.classList.remove("fade-out");

        // ⬅ 여기서 페이드 인만!
        raidOverlay.classList.add("show");

        const fadeDuration = 800;   // 페이드 인 0.8초
        const holdDuration = 700;   // 완전히 까맣게 된 후 글자 잠깐 보여주는 시간

        // 화면이 이미 까맣게 된 뒤에 페이지 이동
        setTimeout(() => {
            window.location.href = raidUrl;
        }, fadeDuration + holdDuration); // 0.8 + 0.7 = 1.5초 후 이동
    });
}


// 자동 연결
connect();
