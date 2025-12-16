const token = localStorage.getItem("token");

if (!token) {
    alert("로그인이 필요합니다!");
    location.href = "/login";
}

const roomId = new URLSearchParams(window.location.search).get("room");

if (!roomId) {
    alert("잘못된 방 접근입니다!");
    location.href = "/cmind/rooms"; // 방 선택 화면으로 되돌리기
}

const nickname = localStorage.getItem("nickname");

const socket = new WebSocket(`ws://${window.location.host}/ws/chat?token=${token}&room=${roomId}&nickname=${nickname}`);

const messagesDiv = document.getElementById("messages");

socket.onopen = () => {
    console.log("WebSocket 연결됨");
};

socket.onmessage = function(event) {
    const data = JSON.parse(event.data);

    if (data.type === "system") {
        messagesDiv.innerHTML += `<div style="color: gray;"><i>${data.message}</i></div>`;
        return;
    }

    if (data.type === "count") {
        document.getElementById("roomInfo").innerText =
            `현재 인원: ${data.count}명`;
        return;
    }

    if (data.roomId !== roomId) return;

    const nickname = data.nickname;
    const message = data.message;

    messagesDiv.innerHTML += `<div><b>${nickname}</b>: ${message}</div>`;
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
};

function sendMessage() {
    const text = document.getElementById("msgInput").value;
    if (text.trim() === "") return;

    const nickname = localStorage.getItem("nickname");

    const msgObj = {
        type: "chat",
        roomId: roomId,
        nickname: nickname,
        message: text
    };

    socket.send(JSON.stringify(msgObj));
    document.getElementById("msgInput").value = "";
}