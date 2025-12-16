package com.cmind.cmind.config;

import com.cmind.cmind.controller.GameController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatHandler chatHandler;
    private final DrawHandler drawHandler;
    private final GameController gameController;

    public WebSocketConfig(ChatHandler chatHandler, DrawHandler drawHandler, GameController gameController) {
        this.chatHandler = chatHandler;
        this.drawHandler = drawHandler;
        this.gameController = gameController;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 채팅 웹소켓
        registry.addHandler(chatHandler, "/ws/chat")
                .setAllowedOrigins("*");

        // 그림 웹소켓
        registry.addHandler(drawHandler, "/ws/draw")
                .setAllowedOrigins("*");

        // 게임 웹소켓
        registry.addHandler(gameController, "/ws/game")
                .setAllowedOrigins("*");
    }


}
