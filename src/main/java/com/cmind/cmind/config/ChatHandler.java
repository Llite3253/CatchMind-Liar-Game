package com.cmind.cmind.config;

import com.cmind.cmind.dto.Room;
import com.cmind.cmind.service.RoomService;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private final Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final RoomService roomService;

    public ChatHandler(RoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String roomId = getRoomId(session);
        String nickname = getNickname(session);

        rooms.putIfAbsent(roomId, new CopyOnWriteArrayList<>());
        rooms.get(roomId).add(session);

        roomService.increaseCount(roomId);

        String joinMsg = String.format(
                "{\"type\":\"system\", \"message\":\"%s님이 입장했습니다.\"}",
                nickname
        );

        broadcast(roomId, joinMsg);
        broadcastRoomCount(roomId);

        System.out.println("채팅 WebSocket 연결됨. room = " + roomId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = getRoomId(session);

        for (WebSocketSession s : rooms.get(roomId)) {
            if (s.isOpen()) {
                s.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        String roomId = getRoomId(session);
        String nickname = getNickname(session);

        rooms.get(roomId).remove(session);

        roomService.decreaseCount(roomId);

        roomService.removeRoomIfEmpty(roomId);

        String leaveMsg = String.format(
                "{\"type\":\"system\", \"message\":\"%s님이 퇴장했습니다.\"}",
                nickname
        );

        broadcast(roomId, leaveMsg);
        broadcastRoomCount(roomId);

        System.out.println("채팅 WebSocket 종료됨. room = " + roomId);
    }

    private void broadcast(String roomId, String message) throws IOException {
        for (WebSocketSession s : rooms.get(roomId)) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(message));
            }
        }
    }

    private void broadcastRoomCount(String roomId) throws IOException {
        Optional<Room> roomOpt = roomService.getRooms().stream()
                .filter(r -> r.getId() == Integer.parseInt(roomId))
                .findFirst();

        // 방이 이미 삭제되었다면 브로드캐스트 하지 않음!
        if (roomOpt.isEmpty()) return;

        int count = roomOpt.get().getCount();

        String msg = "{\"type\":\"count\",\"roomId\":\"" + roomId + "\", \"count\": " + count + "}";

        for (WebSocketSession s : rooms.get(roomId)) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(msg));
            }
        }
    }

    private String getRoomId(WebSocketSession session) {
        String query = session.getUri().getQuery(); // ?token=123&room=1
        for (String part : query.split("&")) {
            if (part.startsWith("room=")) {
                return part.substring(5);
            }
        }
        return "default";
    }

    private String getNickname(WebSocketSession session) {
        String query = session.getUri().getQuery();
        for (String q : query.split("&")) {
            if (q.startsWith("nickname=")) {
                return q.substring("nickname=".length());
            }
        }
        return "default";
    }
}