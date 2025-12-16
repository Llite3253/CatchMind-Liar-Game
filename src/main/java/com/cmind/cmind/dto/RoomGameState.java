package com.cmind.cmind.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RoomGameState {
    private String roomId;

    private List<String> players = new ArrayList<>();
    private String liar;
    private String answerTopic;
    private String fakeTopic;

    // 진행 상태
    private int turnIndex = 0;
    private boolean isGameStarted = false;
    private boolean isVoting = false;
    private boolean isGuessing = false;
    private boolean isLiarCaught = false;

    // 투표 목록
    private Map<String, String> votes = new HashMap<>();
}
