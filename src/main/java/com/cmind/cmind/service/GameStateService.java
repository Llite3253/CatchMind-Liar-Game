package com.cmind.cmind.service;

import com.cmind.cmind.dto.RoomGameState;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameStateService {
    private final Map<String, RoomGameState> gameStates = new ConcurrentHashMap<>();

    public RoomGameState getOrCreate(String roomId) {
        return gameStates.computeIfAbsent(roomId, id -> {
            RoomGameState state = new RoomGameState();
            state.setRoomId(id);
            return state;
        });
    }

    public void remove(String roomId) {
        gameStates.remove(roomId);
    }

    public void addPlayer(String roomId, String nickname) {
        RoomGameState state = getOrCreate(roomId);
        if(!state.getPlayers().contains(nickname)) {
            state.getPlayers().add(nickname);
        }
    }

    public void removePlayer(String roomId, String nickname) {
        RoomGameState state = getOrCreate(roomId);
        state.getPlayers().remove(nickname);

        if(state.getPlayers().isEmpty()) {
            remove(roomId);
        }
    }

    // 게임시작: 라이어/주제 배정
    public void startGame(String roomId, String answerTopic, String fakeTopic) {
        RoomGameState s = getOrCreate(roomId);

        List<String> p = s.getPlayers();
        Collections.shuffle(p);
        s.setLiar(p.get(0));

        s.setAnswerTopic(answerTopic);
        s.setFakeTopic(fakeTopic);
        s.setTurnIndex(0);
        s.setGameStarted(true);
        s.setVoting(false);
        s.setGuessing(false);

        s.setVotes(new HashMap<>());
    }
}
