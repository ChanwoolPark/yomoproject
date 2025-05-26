document.getElementById('profileImage').addEventListener('change', function(event) {
    const input = event.target;
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const preview = document.getElementById('preview');
            preview.src = e.target.result;
            preview.style.display = 'block';
        };
        reader.readAsDataURL(input.files[0]);
    }
});

// 아이디 유효성 및 중복확인
document.getElementById('btnUsernameCheck').onclick = function() {
    const username = document.getElementById('username').value;
    if (!/^[a-zA-Z][a-zA-Z0-9]{5,14}$/.test(username)) {
        document.getElementById('usernameMsg').innerText = "6~15자, 영문 시작, 숫자/영문 가능";
        return;
    }
    fetch(`/api/user/check-username?username=${encodeURIComponent(username)}`)
        .then(res => res.json())
        .then(data => {
            document.getElementById('usernameMsg').innerText = data ? "이미 존재하는 아이디입니다." : "사용 가능한 아이디입니다.";
        });
};

document.getElementById('username').addEventListener('input', function() {
    document.getElementById('usernameMsg').innerText = ""; // 입력 변경시 메시지 초기화
});

// 비밀번호 유효성
document.getElementById('password').addEventListener('input', function() {
    const val = this.value;
    if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[~!@#$%^&*()_+`{}\[\]:;"'<>,.?\/\\|]).{8,20}$/.test(val)) {
        document.getElementById('passwordMsg').innerText = "8~20자, 영문/숫자/특수문자 포함";
    } else {
        document.getElementById('passwordMsg').innerText = "";
    }
});

// 비밀번호 확인 일치
document.getElementById('passwordCheck').addEventListener('input', function() {
    if (this.value !== document.getElementById('password').value) {
        document.getElementById('passwordCheckMsg').innerText = "비밀번호가 일치하지 않습니다.";
    } else {
        document.getElementById('passwordCheckMsg').innerText = "";
    }
});

// 닉네임 유효성
document.getElementById('nickname').addEventListener('blur', function() {
    const val = this.value;
    const msg = document.getElementById('nicknameMsg');

    if (!/^[a-zA-Z0-9가-힣]{2,20}$/.test(val)) {
        msg.innerText = "특수문자 없이 2~20자";
        return;
    }

    // 중복 체크
    fetch(`/api/user/check-nickname?nickname=${encodeURIComponent(val)}`)
        .then(res => res.json())
        .then(data => {
            if (data) {
                msg.innerText = "이미 존재하는 닉네임입니다.";
            } else {
                msg.innerText = ""; // 중복 없음
            }
        });
});


// 이메일 중복확인 (입력 변경시 자동)
document.getElementById('email').addEventListener('blur', function() {
    const val = this.value;
    fetch(`/api/user/check-email?email=${encodeURIComponent(val)}`)
        .then(res => res.json())
        .then(data => {
            document.getElementById('emailMsg').innerText = data ? "이미 존재하는 이메일입니다." : "";
        });
});

// 휴대폰 중복확인 (입력 변경시 자동)
document.getElementById('phone').addEventListener('blur', function() {
    const val = this.value;
    fetch(`/api/user/check-phone?phone=${encodeURIComponent(val)}`)
        .then(res => res.json())
        .then(data => {
            document.getElementById('phoneMsg').innerText = data ? "이미 존재하는 휴대폰입니다." : "";
        });
});

document.querySelector('form').onsubmit = function(e) {
    // 비밀번호, 아이디 등 추가 유효성 확인 (필요시)
    // 만약 조건 안맞으면 e.preventDefault();
};

document.querySelector('form').onsubmit = function(e) {
    // 각 필드의 메시지가 있으면(=유효성 통과 못했으면) 제출 막음
    let fail = false;
    // 아이디 검사
    if (!/^[a-zA-Z][a-zA-Z0-9]{5,14}$/.test(document.getElementById('username').value)
        || document.getElementById('usernameMsg').innerText.indexOf('사용 가능한') < 0) {
        document.getElementById('usernameMsg').innerText = "아이디를 다시 확인하세요";
        fail = true;
    }

    // 비밀번호 검사
    let pwVal = document.getElementById('password').value;
    if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[~!@#$%^&*()_+`{}\[\]:;"'<>,.?\/\\|]).{8,20}$/.test(pwVal)) {
        document.getElementById('passwordMsg').innerText = "비밀번호를 다시 확인하세요";
        fail = true;
    }

    // 비밀번호 일치
    if (pwVal !== document.getElementById('passwordCheck').value) {
        document.getElementById('passwordCheckMsg').innerText = "비밀번호가 일치하지 않습니다.";
        fail = true;
    }

    // 닉네임 검사
    if (!/^[a-zA-Z0-9가-힣]{2,20}$/.test(document.getElementById('nickname').value)) {
        document.getElementById('nicknameMsg').innerText = "닉네임을 다시 확인하세요";
        fail = true;
    }

    // 이메일 중복 메시지
    if (document.getElementById('emailMsg').innerText) {
        fail = true;
    }

    // 휴대폰 중복 메시지
    if (document.getElementById('phoneMsg').innerText) {
        fail = true;
    }

    if (fail) {
        e.preventDefault();
        alert('모든 항목을 올바르게 입력해주세요.');
    }
};

