document.addEventListener("DOMContentLoaded", () => {
    const portrait = document.getElementById("char-portrait");
    const vnDialog = document.getElementById("vn-dialog");
    const toggleBtn = document.getElementById("skill-badge-btn");
    const skillPanel = document.getElementById("skill-panel");
    const stateEl = document.getElementById("equip-state");

    if (portrait && vnDialog) {
        portrait.addEventListener("click", () => vnDialog.classList.toggle("show"));
    }
    if (toggleBtn && skillPanel) {
        toggleBtn.addEventListener("click", () => skillPanel.classList.toggle("open"));
    }

    // ====== 로컬 상태(현재 장착 코드) ======
    const state = {
        slot1: stateEl?.dataset.slot1 || "",
        slot2: stateEl?.dataset.slot2 || "",
        busy: false,
    };

    // 최초 로딩 시 버튼 상태 맞추기
    syncEquipButtonsDisabled();

    // ====== 이벤트 위임 ======
    document.addEventListener("click", async (e) => {
        const equipBtn = e.target.closest(".equip-btn");
        const unequipBtn = e.target.closest(".unequip-btn");

        if (!equipBtn && !unequipBtn) return;

        // 연타/중복요청 방지
        if (state.busy) return;

        // disabled 버튼은 아예 무시 (disabled면 클릭 이벤트 자체가 안 오지만 안전빵)
        if (equipBtn && equipBtn.disabled) return;

        try {
            state.busy = true;

            if (equipBtn) {
                const skillCode = equipBtn.dataset.skillCode;

                // 프론트에서 1차 차단(이미 장착/슬롯 꽉참이면 요청 안 보냄)
                if (!skillCode) return;
                if (skillCode === state.slot1 || skillCode === state.slot2) return;
                if (state.slot1 && state.slot2) return;

                // 요청 중엔 버튼 잠궈서 추가 연타 차단
                equipBtn.disabled = true;

                const res = await fetch("/my-info/skill/equip", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ skillCode }),
                });

                if (!res.ok) {
                    const msg = await res.text();
                    alert("장착 실패: " + msg);
                    return;
                }

                const data = await res.json();
                applyServerState(data);
                updatePresetUI(data);
                return;
            }

            if (unequipBtn) {
                const slot = unequipBtn.dataset.slot;
                if (!slot) return;

                // 요청 중엔 버튼 잠궈서 연타 차단
                unequipBtn.disabled = true;

                const res = await fetch("/my-info/skill/unequip", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ slot }),
                });

                if (!res.ok) {
                    const msg = await res.text();
                    alert("해제 실패: " + msg);
                    return;
                }

                const data = await res.json();
                applyServerState(data);
                updatePresetUI(data);
                return;
            }
        } finally {
            state.busy = false;
            // 어떤 경우든 패널은 유지하고 버튼 상태는 다시 계산
            if (skillPanel) skillPanel.classList.add("open");
            syncEquipButtonsDisabled();
        }
    });

    // ====== 서버 응답 → state 반영 ======
    function applyServerState(data) {
        state.slot1 = data?.slot1?.code || "";
        state.slot2 = data?.slot2?.code || "";

        if (stateEl) {
            stateEl.dataset.slot1 = state.slot1;
            stateEl.dataset.slot2 = state.slot2;
        }

        console.log("[STATE]", { slot1: state.slot1, slot2: state.slot2 });
    }

    // ====== 슬롯 UI 갱신 ======
    function updatePresetUI(data) {
        const slot1El = document.querySelector("[data-slot='1'] .preset-slot-body");
        const slot2El = document.querySelector("[data-slot='2'] .preset-slot-body");
        if (!slot1El || !slot2El) return;

        slot1El.innerHTML = data.slot1
            ? `
        <div class="preset-icon" style="background-image:url('${data.slot1.imageUrl || ""}')"></div>
        <div class="preset-text">
          <div>${escapeHtml(data.slot1.name || "")}</div>
          <small>[${escapeHtml(data.slot1.tag || "")} / 쿨:${data.slot1.cooldown ?? ""}]</small>
        </div>
        <button type="button" class="unequip-btn" data-slot="1">해제</button>
      `
            : `
        <div class="preset-icon"></div>
        <div class="preset-text">스킬을 선택하세요</div>
      `;

        slot2El.innerHTML = data.slot2
            ? `
        <div class="preset-icon" style="background-image:url('${data.slot2.imageUrl || ""}')"></div>
        <div class="preset-text">
          <div>${escapeHtml(data.slot2.name || "")}</div>
          <small>[${escapeHtml(data.slot2.tag || "")} / 쿨:${data.slot2.cooldown ?? ""}]</small>
        </div>
        <button type="button" class="unequip-btn" data-slot="2">해제</button>
      `
            : `
        <div class="preset-icon"></div>
        <div class="preset-text">스킬을 선택하세요</div>
      `;
    }

    // ====== 장착 버튼 disabled 동기화 ======
    function syncEquipButtonsDisabled() {
        const bothFull = !!state.slot1 && !!state.slot2;

        document.querySelectorAll(".equip-btn").forEach((btn) => {
            const code = btn.dataset.skillCode;

            // 기본적으로는 활성화
            btn.disabled = false;

            // 슬롯 두개 다 차면 전부 비활성화
            if (bothFull) {
                btn.disabled = true;
                return;
            }

            // 이미 장착된 스킬이면 비활성화
            if (code && (code === state.slot1 || code === state.slot2)) {
                btn.disabled = true;
                return;
            }

            // 요청 중(busy)이면 전체 잠궈도 됨(원하면)
            if (state.busy) {
                btn.disabled = true;
            }
        });
    }

    // 아주 간단한 XSS 방지(이름/태그 표시용)
    function escapeHtml(str) {
        return String(str)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
});
