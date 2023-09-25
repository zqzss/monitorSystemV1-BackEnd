package com.seewin.schedule;

import com.alibaba.fastjson.JSON;
import com.seewin.entity.MonitorDataResult;
import com.seewin.entity.MonitorItem;
import com.seewin.entity.Result;
import com.seewin.mapper.*;
import com.seewin.service.EmailService;
import com.seewin.service.MonitorDataService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@EnableAsync
public class MyScheduledTask  {
    private Set<WebSocketSession> sessions = new HashSet<>();
    //    @Value("${cron.getMonitorDataCron}")
//    private String getMonitorDataCron;
//    @Value("${cron.deleteMonitorDataCron}")
//    private String deleteMonitorDataCron;
    @Value("${reConnectNumber}")
    private Integer reConnectNumber;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private MonitorItemMapper monitorItemMapper;
    @Autowired
    private MonitorDataService monitorDataService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private MonitorAlarmMapper monitorAlarmMapper;
    @Autowired
    private MonitorDataMapper monitorDataMapper;
    @Autowired
    private MonitorTypeMapper monitorTypeMapper;
    @Autowired
    private NoticeTypeMapper noticeTypeMapper;
    @Autowired
    private NoticeItemMapper noticeItemMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    GetMonitorDataRunable getMonitorDataRunable;

    public void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public void sendMonitorDataToAllSessions(Result result) throws IOException {
        for (WebSocketSession session : sessions) {
            TextMessage textMessage = new TextMessage(JSON.toJSONBytes(result)); // 假设 toJSON() 方法将结果转换为 JSON 字符串
            session.sendMessage((WebSocketMessage<?>) textMessage);

        }
    }

    @Scheduled(cron = "${cron.deleteMonitorDataCron}")
    public void deleteMonitorDataTask() {
        //            删除当前一天前的数据
        LocalDate currentDate = LocalDate.now();
        LocalDate oneDayAgo = currentDate.minusDays(1);
        Date oneDayAgoDate = Date.from(oneDayAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
        monitorDataMapper.deleteOldData(oneDayAgoDate);
    }

    @Scheduled(cron = "${cron.getMonitorDataCron}")
    public void getMonitorDataTask() throws Exception {
        List<MonitorItem> monitorItems = monitorItemMapper.selectList(null);
        List<MonitorDataResult> monitorDataResults = new ArrayList<>();
        for (MonitorItem monitorItem : monitorItems) {
//            log.info(" monitorItem1: "+monitorItem.toString());

            getMonitorDataRunable.run(monitorItem);
        }
    }

}
