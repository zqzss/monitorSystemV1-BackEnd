package com.seewin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
//public class WebSocketConfig implements WebSocketConfigurer {
public class WebSocketConfig {
    //    private final MyWebSocketHandler myWebSocketHandler;
//
//    public WebSocketConfig(MyWebSocketHandler myWebSocketHandler) {
//
//        this.myWebSocketHandler = myWebSocketHandler;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(myWebSocketHandler,"/ws").setAllowedOrigins("*");
//    }
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
