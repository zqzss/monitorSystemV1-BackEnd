//package com.seewin.common;
//
//import com.seewin.schedule.MyScheduledTask;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//@Component
//public class MyWebSocketHandler extends TextWebSocketHandler {
//    private final MyScheduledTask host;
//
//    public MyWebSocketHandler(MyScheduledTask host) {
//
//        this.host = host;
//    }
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        host.addSession(session);
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        // 处理接收到的消息，如果需要的话
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        host.removeSession(session);
//    }
//}
