const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const resultEl = document.getElementById("result");
const loginBtn = document.getElementById("login-btn");

async function login() {
    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    // 메시지 초기화
    resultEl.textContent = "";
    resultEl.classList.remove("error", "success");

    if (!username || !password) {
        resultEl.textContent = "아이디와 비밀번호를 입력하세요.";
        resultEl.classList.add("error");
        return;
    }

    try {
        const res = await fetch("/api/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (!res.ok) {
            const text = await res.text();
            resultEl.textContent = "로그인 실패: " + text;
            resultEl.classList.add("error");
            return;
        }

        const data = await res.json();
        resultEl.textContent = "로그인 성공: " + data.username + " (" + data.role + ")";
        resultEl.classList.add("success");

        // 잠깐 보여주고 내 정보 페이지로 이동
        setTimeout(() => {
            if (data.role == "ADMIN")
            {
                window.location.href = "/admin";
            } else
            {
                window.location.href = "/my-page";
            }
        }, 400);
    } catch (e) {
        console.error(e);
        resultEl.textContent = "요청 중 오류가 발생했습니다.";
        resultEl.classList.add("error");
    }
}

// 버튼 클릭
loginBtn.addEventListener("click", login);

// 엔터키 입력 시 로그인
[usernameInput, passwordInput].forEach((input) => {
    input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            login();
        }
    });
});
