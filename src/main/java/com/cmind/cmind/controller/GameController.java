package com.cmind.cmind.controller;

import com.cmind.cmind.dto.GameMessage;
import com.cmind.cmind.dto.RoomGameState;
import com.cmind.cmind.service.GameStateService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class GameController extends TextWebSocketHandler {
    private final GameStateService gameService;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public GameController(GameStateService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getParam(session, "room");
        String nickname = getParam(session, "nickname");

        roomSessions.computeIfAbsent(roomId, r -> new CopyOnWriteArrayList<>());
        roomSessions.get(roomId).add(session);

        gameService.addPlayer(roomId, nickname);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        GameMessage gm = mapper.readValue(msg.getPayload(), GameMessage.class);

        switch (gm.getType()) {
            case "startGame":
                handleStartGame(gm);
                break;

            case "nextTurn":
                handleNextTurn(gm);
                break;

            case "vote":
                handleVote(gm);
                break;

            case "guessAnswer":
                handleGuess(gm);
                break;
        }
    }

    /* --- 게임 로직 --- */
    private void handleStartGame(GameMessage gm) throws Exception {
        String roomId = gm.getRoomId();
        RoomGameState state = gameService.getOrCreate(roomId);

        broadcast(roomId, json(
                "type", "gameStarted"
        ));

        broadcast(roomId, json(
                "type", "clear"
        ));

        String answer = "토마토";
        String fake = "사과";

        gameService.startGame(roomId, answer, fake);

        broadcastTo(roomId, state.getLiar(), json(
                "type", "liarNotice",
                "topic", state.getFakeTopic()
        ));

        for(String p : state.getPlayers()) {
            if(!p.equals(state.getLiar())) {
                broadcastTo(roomId, p, json(
                        "type", "topic",
                        "topic", state.getAnswerTopic()
                ));
            }
        }

        handleNextTurn(gm);
    }

    private void handleNextTurn(GameMessage gm) throws Exception {
        String roomId = gm.getRoomId();
        RoomGameState st = gameService.getOrCreate(roomId);

        if(st.getTurnIndex() >= st.getPlayers().size()) {
            startVoting(roomId);
            return;
        }

        String turnPlayer = st.getPlayers().get(st.getTurnIndex());

        broadcast(roomId, json(
                "type", "turn",
                "player", turnPlayer,
                "time", "10"
        ));

        st.setTurnIndex(st.getTurnIndex() + 1);
    }

    private void startVoting(String roomId) throws Exception {
        RoomGameState st = gameService.getOrCreate(roomId);
        st.setVoting(true);

        broadcast(roomId, json(
                "type", "voteStart",
                "players", String.join(",", st.getPlayers())
        ));
    }

    private void handleVote(GameMessage gm) throws Exception {
        RoomGameState st = gameService.getOrCreate(gm.getRoomId());

        st.getVotes().put(gm.getNickname(), gm.getVoteTarget());

        if(st.getVotes().size() == st.getPlayers().size()) {
            computeVoteResult(gm.getRoomId());
        }
    }

    private void computeVoteResult(String roomId) throws Exception {
        RoomGameState st = gameService.getOrCreate(roomId);

        Map<String, Integer> count = new HashMap<>();
        for(String target : st.getVotes().values()) {
            count.put(target, count.getOrDefault(target, 0) + 1);
        }

        String voted = count.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();

        boolean correct = voted.equals(st.getLiar());

        st.setLiarCaught(correct);

        st.setVoting(false);
        st.setGuessing(true);

        broadcast(roomId, json(
                "type", "voteResult",
                "selected", voted,
                "isLiar", Boolean.toString(correct)
        ));

        startGuess(roomId);
    }

    private void startGuess(String roomId) throws Exception {
        RoomGameState st = gameService.getOrCreate(roomId);

        broadcastTo(roomId, st.getLiar(),json(
                "type", "guessStart",
                "message", "당신은 라이어입니다. 정답을 입력하세요."
        ));
    }

    private void handleGuess(GameMessage gm) throws Exception {
        RoomGameState st = gameService.getOrCreate(gm.getRoomId());

        if(!gm.getNickname().equals(st.getLiar())) {
            return;
        }

        boolean correct = gm.getAnswerInput().equals(st.getAnswerTopic());

        String result;

        if (st.isLiarCaught()) {
            result = correct ? "무승부" : "플레이어 승리";
        } else {
            result = correct ? "라이어 승리" : "무승부";
        }

        broadcast(gm.getRoomId(), json(
                "type", "finalResult",
                "result", result
        ));
    }

    /* --- 유틸 --- */
    private void broadcast(String roomId, String msg) throws Exception {
        for(WebSocketSession s : roomSessions.get(roomId)) {
            if(s.isOpen())
                s.sendMessage(new TextMessage(msg));
        }
    }

    private void broadcastTo(String roomId, String nickname, String msg) throws Exception {
        for(WebSocketSession s : roomSessions.get(roomId)) {
            String n = getParam(s, "nickname");
            if(nickname.equals(n)) {
                s.sendMessage(new TextMessage(msg));
            }
        }
    }

    private String getParam(WebSocketSession s, String key) {
        String[] q = s.getUri().getQuery().split("&");
        for(String p : q) {
            if(p.startsWith(key+"="))
                return p.substring(key.length()+1);
        }
        return null;
    }

    private String json(String... kv) {
        StringBuilder b = new StringBuilder("{");
        for(int i=0; i<kv.length; i+=2) {
            b.append("\"").append(kv[i]).append("\":\"").append(kv[i+1]).append("\"");
            if(i < kv.length-2) b.append(",");
        }
        b.append("}");
        return b.toString();
    }
}
