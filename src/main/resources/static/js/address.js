// 1) 팝업 띄우기
document.getElementById('btnPostcode').addEventListener('click', () => {
    window.open('/juso/popup',
        'addrPopup',
        'width=600,height=600,scrollbars=yes');
});

// 2) 팝업에서 메시지 받기
window.addEventListener('message', (e) => {
    if (e.origin !== window.origin) return;
    const { zipCode, roadAddr, jibunAddr } = e.data;
    document.getElementById('zipCode').value  = zipCode;
    document.getElementById('roadAddr').value = roadAddr;
    document.getElementById('jibunAddr').value= jibunAddr;
});
