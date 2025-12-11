document.addEventListener("DOMContentLoaded", () => {
    const portrait = document.getElementById("char-portrait");
    const vnDialog = document.getElementById("vn-dialog");
    const toggleBtn = document.getElementById("vn-toggle-btn");
    const skillPanel = document.getElementById("skill-panel");

    // 캐릭터 이미지 클릭 → VN 말풍선 on/off
    if (portrait && vnDialog) {
        portrait.addEventListener("click", () => {
            vnDialog.classList.toggle("show");
        });
    }

    // 유일한 버튼 → 스킬 패널 슬라이드 토글
    if (toggleBtn && skillPanel) {
        toggleBtn.addEventListener("click", () => {
            skillPanel.classList.toggle("open");
        });
    }
});
