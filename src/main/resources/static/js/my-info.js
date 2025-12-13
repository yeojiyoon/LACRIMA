document.addEventListener("DOMContentLoaded", () => {

    /* =========================
       기본 DOM
    ========================= */
    const portrait   = document.getElementById("char-portrait");
    const vnDialog   = document.getElementById("vn-dialog");
    const vnText     = document.getElementById("vn-dialog-text");

    const skillBtn   = document.getElementById("skill-badge-btn");
    const skillPanel = document.getElementById("skill-panel");
    const stateEl    = document.getElementById("equip-state");

    // 🔥 중요: 먼저 선언
    const settingsWin = document.getElementById("settings-window");
    const settingBtn  = document.getElementById("setting-badge-btn");
    const backdrop    = document.getElementById("settings-backdrop");
    const closeBtn    = document.getElementById("settings-close-btn");
    const cancelBtn   = document.getElementById("settings-cancel-btn");

    // settings inputs
    const one1 = document.getElementById("one1");
    const one2 = document.getElementById("one2");
    const one3 = document.getElementById("one3");

    // save/hint (있으면 쓰고, 없으면 그냥 무시)
    const saveBtn = document.getElementById("settings-save-btn");
    const hintEl  = document.getElementById("settings-hint");

    function setHint(msg, type){
        if (!hintEl) return;
        hintEl.classList.remove("ok","err");
        if (type) hintEl.classList.add(type);
        hintEl.textContent = msg || "";
    }

    /* =========================
       ✅ DB(dataset) 동기화 유틸
    ========================= */
    function getOneLinersFromDataset(){
        if (!settingsWin) return [];
        const a = settingsWin.dataset.one1;
        const b = settingsWin.dataset.one2;
        const c = settingsWin.dataset.one3;
        return [a,b,c].filter(v => v && v.trim().length > 0);
    }

    // 🔥 디버그: 여기서 찍히는지 확인 가능
    // (안 찍히면 JS 파일 자체가 안 붙은거임)
    console.log("[settings dataset]", {
        one1: settingsWin?.dataset?.one1,
        one2: settingsWin?.dataset?.one2,
        one3: settingsWin?.dataset?.one3,
    });

    /* =========================
       VN 랜덤 한마디
    ========================= */
    function pickRandomOneLiner() {
        if (!vnText) return;

        const list = getOneLinersFromDataset();
        if (list.length === 0) {
            // dataset이 비면 기존 텍스트 유지(서버가 박아준 randomOneLiner)
            return;
        }

        vnText.textContent = list[Math.floor(Math.random() * list.length)];
    }

    /* =========================
       VN 토글 & 위치
    ========================= */
    function toggleVnAtClick(e) {
        if (!vnDialog) return;

        const x = e.clientX;
        const y = e.clientY;

        // 같은 위치 클릭 → 닫기
        if (vnDialog.classList.contains("show")) {
            const curLeft = parseFloat(vnDialog.style.left || "0");
            const curTop  = parseFloat(vnDialog.style.top  || "0");
            if (Math.hypot(x - curLeft, y - curTop) < 20) {
                vnDialog.classList.remove("show");
                return;
            }
        }

        pickRandomOneLiner();
        vnDialog.classList.add("show");

        requestAnimationFrame(() => {
            const rect = vnDialog.getBoundingClientRect();
            const maxLeft = window.innerWidth  - rect.width  - 10;
            const maxTop  = window.innerHeight - rect.height - 10;

            vnDialog.style.left =
                `${Math.max(10, Math.min(maxLeft, x))}px`;
            vnDialog.style.top  =
                `${Math.max(10, Math.min(maxTop,  y))}px`;
        });
    }

    /* =========================
       VN 클릭 이벤트
    ========================= */
    portrait?.addEventListener("click", toggleVnAtClick);

    vnDialog?.addEventListener("click", (e) => {
        e.stopPropagation();
        pickRandomOneLiner();
    });

    document.addEventListener("click", (e) => {
        if (e.target.closest("#vn-dialog")) return;
        if (e.target.closest("#char-portrait")) return;
        vnDialog?.classList.remove("show");
    });

    /* =========================
       스킬 패널
    ========================= */
    skillBtn?.addEventListener("click", () => {
        skillPanel?.classList.toggle("open");
    });

    /* =========================
       스킬 장착 로직
    ========================= */
    const state = {
        slot1: stateEl?.dataset.slot1 || "",
        slot2: stateEl?.dataset.slot2 || "",
        busy: false,
    };

    syncEquipButtonsDisabled();

    document.addEventListener("click", async (e) => {
        const equipBtn = e.target.closest(".equip-btn");
        const unequipBtn = e.target.closest(".unequip-btn");
        if (!equipBtn && !unequipBtn) return;
        if (state.busy) return;

        try {
            state.busy = true;

            if (equipBtn && !equipBtn.disabled) {
                const skillCode = equipBtn.dataset.skillCode;
                if (!skillCode) return;
                if (skillCode === state.slot1 || skillCode === state.slot2) return;
                if (state.slot1 && state.slot2) return;

                equipBtn.disabled = true;

                const res = await fetch("/my-info/skill/equip", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ skillCode }),
                });

                if (!res.ok) return alert(await res.text());

                const data = await res.json();
                applyServerState(data);
                updatePresetUI(data);
            }

            if (unequipBtn) {
                const res = await fetch("/my-info/skill/unequip", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ slot: unequipBtn.dataset.slot }),
                });

                if (!res.ok) return alert(await res.text());

                const data = await res.json();
                applyServerState(data);
                updatePresetUI(data);
            }
        } finally {
            state.busy = false;
            syncEquipButtonsDisabled();
            skillPanel?.classList.add("open");
        }
    });

    function applyServerState(data) {
        state.slot1 = data?.slot1?.code || "";
        state.slot2 = data?.slot2?.code || "";
        if (stateEl) {
            stateEl.dataset.slot1 = state.slot1;
            stateEl.dataset.slot2 = state.slot2;
        }
    }

    function updatePresetUI(data) {
        ["1","2"].forEach(slot => {
            const el = document.querySelector(`[data-slot='${slot}'] .preset-slot-body`);
            if (!el) return;

            const s = data[`slot${slot}`];
            el.innerHTML = s
                ? `
                <div class="preset-icon" style="background-image:url('${s.imageUrl || ""}')"></div>
                <div class="preset-text">
                    <div>${escapeHtml(s.name)}</div>
                    <small>[${escapeHtml(s.tag)} / 쿨:${s.cooldown}]</small>
                </div>
                <button type="button" class="unequip-btn" data-slot="${slot}">해제</button>
                `
                : `
                <div class="preset-icon"></div>
                <div class="preset-text">스킬을 선택하세요</div>
                `;
        });
    }

    function syncEquipButtonsDisabled() {
        const both = state.slot1 && state.slot2;
        document.querySelectorAll(".equip-btn").forEach(btn => {
            const code = btn.dataset.skillCode;
            btn.disabled = both || code === state.slot1 || code === state.slot2 || state.busy;
        });
    }

    function escapeHtml(str) {
        return String(str)
            .replaceAll("&","&amp;")
            .replaceAll("<","&lt;")
            .replaceAll(">","&gt;")
            .replaceAll('"',"&quot;")
            .replaceAll("'","&#039;");
    }

    /* =========================
       ✅ SETTING 창 열기/닫기 + 프리필
    ========================= */
    function prefillSettingsInputsFromDataset(){
        if (!settingsWin) return;
        if (one1) one1.value = settingsWin.dataset.one1 || "";
        if (one2) one2.value = settingsWin.dataset.one2 || "";
        if (one3) one3.value = settingsWin.dataset.one3 || "";
    }

    function openSettings(){
        if (!settingsWin || !backdrop) return;
        // 🔥 DB 내려온 값을 input에 확실히 채움
        prefillSettingsInputsFromDataset();

        settingsWin.hidden = false;
        backdrop.hidden = false;
        setHint("", null);
    }

    function closeSettings(){
        if (!settingsWin || !backdrop) return;
        settingsWin.hidden = true;
        backdrop.hidden = true;
        setHint("", null);
    }

    settingBtn?.addEventListener("click", (e) => {
        e.stopPropagation();
        openSettings();
    });

    closeBtn?.addEventListener("click", closeSettings);
    cancelBtn?.addEventListener("click", closeSettings);
    backdrop?.addEventListener("click", closeSettings);

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && settingsWin && !settingsWin.hidden) closeSettings();
    });

    /* =========================
       ✅ 저장(있으면) → DB 저장 후 dataset 갱신
       (서버 엔드포인트가 이미 있다면 그대로 동작)
    ========================= */
    saveBtn?.addEventListener("click", async () => {
        try{
            saveBtn.disabled = true;
            setHint("저장 중…", null);

            const payload = {
                oneLiner1: (one1?.value || "").trim(),
                oneLiner2: (one2?.value || "").trim(),
                oneLiner3: (one3?.value || "").trim(),
            };

            const res = await fetch("/my-info/settings/one-liners", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (!res.ok){
                setHint("저장 실패: " + (await res.text()), "err");
                return;
            }

            const data = await res.json();

            // 🔥 dataset 갱신 (=> VN 랜덤도 즉시 바뀜)
            settingsWin.dataset.one1 = data.oneLiner1 || "";
            settingsWin.dataset.one2 = data.oneLiner2 || "";
            settingsWin.dataset.one3 = data.oneLiner3 || "";

            setHint("저장 완료.", "ok");
        } catch (err){
            setHint("저장 실패(에러). 콘솔 확인", "err");
            console.error(err);
        } finally {
            saveBtn.disabled = false;
        }
    });

    /* =========================
       SETTING 창 드래그 (너 코드 유지)
    ========================= */
    const dragHandle = document.getElementById("settings-drag-handle");
    let dragging = false, sx=0, sy=0, sl=0, st=0;

    dragHandle?.addEventListener("mousedown", (e) => {
        if (!settingsWin || settingsWin.hidden) return;
        dragging = true;
        const r = settingsWin.getBoundingClientRect();
        sx = e.clientX; sy = e.clientY;
        sl = r.left; st = r.top;
        e.preventDefault();
    });

    document.addEventListener("mousemove", (e) => {
        if (!dragging || !settingsWin) return;
        settingsWin.style.left = `${sl + e.clientX - sx}px`;
        settingsWin.style.top  = `${st + e.clientY - sy}px`;
    });

    document.addEventListener("mouseup", () => dragging = false);

});
