package com.cmind.cmind.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DrawHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final Map<String, List<String>> roomHistory = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomId(session);

        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());
        roomHistory.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());

        roomSessions.get(roomId).add(session);

        for (String msg : roomHistory.get(roomId)) {
            session.sendMessage(new TextMessage(msg));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String roomId = getRoomId(session);
        String msg = message.getPayload();

        roomHistory.get(roomId).add(msg);

        // 같은 방 사용자에게만 브로드캐스트
        for (WebSocketSession s : roomSessions.get(roomId)) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(msg));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomId = getRoomId(session);
        roomSessions.get(roomId).remove(session);

        if (roomSessions.get(roomId).isEmpty()) {
            roomSessions.remove(roomId);
            roomHistory.remove(roomId);
            System.out.println("방 " + roomId + " 그림 히스토리 삭제됨.");
        }
    }

    private String getRoomId(WebSocketSession session) {
        // 예: /ws/draw?token=xxx&room=3
        String query = session.getUri().getQuery();
        if (query == null) return "default";

        for (String q : query.split("&")) {
            if (q.startsWith("room=")) {
                return q.substring(5);
            }
        }
        return "default";
    }
}

