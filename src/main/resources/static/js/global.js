// 전역 클릭 파티클 효과
document.addEventListener("click", (e) => {
    const effect = document.createElement("div");
    effect.classList.add("click-effect");
    effect.style.left = (e.clientX - 20) + "px";
    effect.style.top = (e.clientY - 20) + "px";
    document.body.appendChild(effect);

    setTimeout(() => {
        effect.remove();
    }, 400);
});
