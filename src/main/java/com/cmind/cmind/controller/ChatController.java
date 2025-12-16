package com.cmind.cmind.controller;

import com.cmind.cmind.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ChatController {

    @GetMapping("/chat")
    public ModelAndView chatPage() {
        return new ModelAndView("chat");
    }

    @MessageMapping("/send/{roomId}") // 클라이언트 → 서버
    @SendTo("/topic/messages/{roomId}") // 서버 → 구독중인 클라이언트
    public ChatMessage send(@DestinationVariable String roomId, ChatMessage message) {
        return message;
    }
}
