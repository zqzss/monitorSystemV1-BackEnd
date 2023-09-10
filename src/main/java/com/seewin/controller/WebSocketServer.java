package com.seewin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.seewin.entity.MonitorData;
import com.seewin.entity.MonitorItem;
import com.seewin.mapper.MonitorDataMapper;
import com.seewin.mapper.MonitorItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws")
public class WebSocketServer {
    @Autowired
    private MonitorDataMapper monitorDataMapper;
    @Autowired
    private MonitorItemMapper monitorItemMapper;
    private static ConcurrentHashMap<Session, Map<Integer,Integer>> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 连接建立成功用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("message") String message){
        // 处理接收到的消息

        // 解析消息字符串获取数据
        JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();
        Integer currentPage = jsonObject.get("currentPage").getAsInt();
        Integer pageSize = jsonObject.get("pageSize").getAsInt();
        HashMap<Integer,Integer> page = new HashMap<>();
        page.put(currentPage,pageSize);
        webSocketMap.put(session,page);

        System.out.println("page:"+ page);
        List<MonitorItem> monitorItems = monitorItemMapper.selectList(null);



        IPage<MonitorData> iPage = new Page<>(currentPage,pageSize);

        LambdaQueryWrapper<MonitorData> lambdaQueryWrapper = new LambdaQueryWrapper<>();

    }
    /**
     * 收到客户端消息后调用的方法
     * 参数@param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(Session session,  String message) {
        // 解析消息字符串获取数据
        JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();
        Integer currentPage = jsonObject.get("currentPage").getAsInt();
        Integer pageSize = jsonObject.get("pageSize").getAsInt();
        HashMap page = new HashMap<>();
        page.put(currentPage,pageSize);
        System.out.println("page:" + page);
        webSocketMap.put(session,page);
    }

}
