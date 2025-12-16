const canvas = document.getElementById("board");
const ctx = canvas.getContext("2d");

let drawing = false;
let lastX = null;
let lastY = null;

let currentColor = "#000000";
let currentSize = 4;
let isErasing = false;

// WebSocket
const drawSocket = new WebSocket(`ws://${window.location.host}/ws/draw?token=${window.token}&room=${roomId}`);

const colorPreview = document.getElementById("colorPreview");
const clearBtn = document.getElementById("clearBtn");
// UI 요소
document.getElementById("colorPicker").oninput = (e) => {
    currentColor = e.target.value;
    isErasing = false;

    // 미리보기 색 업데이트
    colorPreview.style.backgroundColor = currentColor;
};

document.getElementById("sizePicker").oninput = (e) => {
    currentSize = Number(e.target.value);
};

// 펜 버튼
document.getElementById("penBtn").onclick = () => {
    isErasing = false;
    colorPreview.style.backgroundColor = currentColor;
    setActiveButton(document.getElementById("penBtn"));
};

// 지우개 버튼
document.getElementById("eraserBtn").onclick = () => {
    if (!window.drawingEnabled) return;
    isErasing = true;
    colorPreview.style.backgroundColor = "#FFFFFF";
    setActiveButton(document.getElementById("eraserBtn"));
};

// 전체 지우기 버튼
document.getElementById("clearBtn").onclick = () => {
    if (!window.drawingEnabled) return;
    clearCanvas();
    drawSocket.send(JSON.stringify({ type: "clear" }));
};

canvas.addEventListener("mousedown", (e) => {
    if (!window.drawingEnabled) return;
    drawing = true;
    lastX = e.offsetX;
    lastY = e.offsetY;
});

canvas.addEventListener("mouseup", () => {
    drawing = false;
    lastX = null;
    lastY = null;
});

canvas.addEventListener("mouseleave", () => {
    drawing = false;
    lastX = null;
    lastY = null;
});

canvas.addEventListener("mousemove", draw);

function draw(e) {
    if (!drawing) return;

    const pos = getCanvasPos(e);

    const data = {
        x1: lastX,
        y1: lastY,
        x2: pos.x,
        y2: pos.y,
        size: currentSize,
        color: isErasing ? "#FFFFFF" : currentColor,
        erase: isErasing
    };

    render(data);
    drawSocket.send(JSON.stringify(data));

    lastX = pos.x;
    lastY = pos.y;
}

drawSocket.onmessage = (msg) => {
    const data = JSON.parse(msg.data);
    
    // 전체 지우기
    if (data.type === "clear") {
        clearCanvas();
        return;
    }

    // 일반 선 그리기
    render(data);
};

function setActiveButton(btn) {
    document.getElementById("penBtn").classList.remove("active");
    document.getElementById("eraserBtn").classList.remove("active");

    btn.classList.add("active");
}

// 캔버스 전체 지우기 함수
function clearCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

function render(p) {
    ctx.strokeStyle = p.color;
    ctx.lineWidth = p.size;
    ctx.lineCap = "round";

    ctx.beginPath();
    ctx.moveTo(p.x1, p.y1);
    ctx.lineTo(p.x2, p.y2);
    ctx.stroke();
}

// 캔버스 크기에 따라 좌표 설정
function getCanvasPos(e) {
    const rect = canvas.getBoundingClientRect();

    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;

    return {
        x: (e.clientX - rect.left) * scaleX,
        y: (e.clientY - rect.top) * scaleY
    };
}
