package com.cmind.cmind.dto;

import lombok.Data;

@Data
public class GameMessage {
    private String type;
    private String roomId;
    private String nickname;
    private String voteTarget;
    private String answerInput;
}
