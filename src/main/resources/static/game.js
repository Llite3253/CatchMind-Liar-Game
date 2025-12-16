const gameSocket = new WebSocket(`ws://${window.location.host}/ws/game?room=${roomId}&nickname=${nickname}`);

// UI 요소
const topicArea = document.getElementById("topicArea");
const turnInfo = document.getElementById("turnInfo");
const timerDiv = document.getElementById("timer");
const voteArea = document.getElementById("voteArea");
const voteButtons = document.getElementById("voteButtons");
const guessArea = document.getElementById("guessArea");
const finalResult = document.getElementById("finalResult");
const startGameBtn = document.getElementById("startGameBtn");

let timerInterval = null;

// WebSocket 이벤트 처리
gameSocket.onmessage = (event) => {
    const data = JSON.parse(event.data);

    switch (data.type) {

        case "liarNotice":
            topicArea.innerHTML = "당신은 라이어입니다.<br>주제: " + data.topic;
            break;

        case "topic":
            topicArea.innerHTML = "주제: " + data.topic;
            break;

        case "turn":
            handleTurn(data);
            break;

        case "voteStart":
            showVoteUI(data.players.split(","));
            break;

        case "voteResult":
            showVoteResult(data);
            finalResult.style.display = "inline-block";
            break;

        case "guessStart":
            guessArea.style.display = "block";
            break;

        case "gameStarted":
            startGameBtn.style.display = "none";
            finalResult.style.display = "none";
            break;

        case "finalResult":
            showFinalResult(data.result);
            startGameBtn.style.display = "inline-block";
            break;

        case "clear":
            clearCanvas();
            break;
    }
};

function startGameClick() {
    try {
        gameSocket.send(JSON.stringify({
            type: "startGame",
            roomId,
            nickname
        }));
    } catch (e) {
        console.error("게임 시작 전송 실패", e);
    }
}

function handleTurn(data) {
    const drawer = data.player;
    const time = parseInt(data.time);

    turnInfo.innerText = `${drawer} 님이 그림을 그리는 중…`;

    if (drawer === nickname) {
        window.drawingEnabled = true;
    } else {
        window.drawingEnabled = false;
    }

    startTimer(time);
}

function startTimer(seconds) {
    if (timerInterval) clearInterval(timerInterval);

    let remain = seconds;
    timerDiv.innerText = "남은 시간: " + remain;

    timerInterval = setInterval(() => {
        remain--;
        timerDiv.innerText = "남은 시간: " + remain;

        if (remain <= 0) {
            clearInterval(timerInterval);

            // 서버에게 다음 턴 요청
            if (window.drawingEnabled) {
                gameSocket.send(JSON.stringify({
                    type: "nextTurn",
                    roomId,
                    nickname
                }));
            }
        }

    }, 1000);
}

function showVoteUI(players) {
    voteArea.style.display = "block";
    voteButtons.innerHTML = "";

    players.forEach(p => {
        const btn = document.createElement("button");
        btn.innerText = p;
        btn.onclick = () => {
            gameSocket.send(JSON.stringify({
                type: "vote",
                roomId,
                nickname,
                voteTarget: p
            }));
            voteArea.style.display = "none";
        };
        voteButtons.appendChild(btn);
    });
}

function showVoteResult(data) {
    if (data.isLiar === "true") {
        finalResult.innerHTML = `${data.selected}님은 라이어 입니다!`;
    } else {
        finalResult.innerHTML = `${data.selected}님은 라이어가 아닙니다.`;
    }
}

function sendGuess() {
    const ans = document.getElementById("guessInput").value;

    gameSocket.send(JSON.stringify({
        type: "guessAnswer",
        roomId,
        nickname,
        answerInput: ans
    }));

    guessArea.style.display = "none";
}

function showFinalResult(result) {
    if (result === "플레이어 승리") {
        finalResult.innerText = "플레이어 승리!";
    } else if (result === "라이어 승리") {
        finalResult.innerText = "라이어 승리!";
    } else {
        finalResult.innerText = "무승부!";
    }

    startGameBtn.style.display = "inline-block";

    voteArea.style.display = "none";  // 투표 창 숨기기
    guessArea.style.display = "none"; // 정답 입력 창 숨기기

    // 타이머 및 턴 정보 초기화
    if (timerInterval) clearInterval(timerInterval);
    timerDiv.innerText = "";
    turnInfo.innerText = "새 게임을 시작하세요!";
    topicArea.innerHTML = "주제 대기중";
}
