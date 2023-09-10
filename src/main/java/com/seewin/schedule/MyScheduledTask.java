package com.seewin.schedule;

import com.alibaba.fastjson.JSON;
import com.seewin.entity.*;
import com.seewin.mapper.*;
import com.seewin.service.EmailService;
import com.seewin.service.MonitorDataService;
import com.seewin.utils.JschUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.BindException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class MyScheduledTask {
    private Set<WebSocketSession> sessions = new HashSet<>();
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
    private String scheduledTime = "3 * * * * *"; // 默认定时器运行时间

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

//    @Scheduled(cron = "*/20 * * * * *")
    public void runScheduledTask() throws Exception {
        List<MonitorItem> monitorItems = monitorItemMapper.selectList(null);
        List<MonitorDataResult> monitorDataResults = new ArrayList<>();
        for (MonitorItem monitorItem : monitorItems) {
            MonitorData monitorData = new MonitorData();

            Integer hostId = monitorItem.getHostId();
            Host host = hostMapper.selectById(hostId);
            if (host.getEnable() != 1) {
                continue;
            }
//          生成当前时间
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);

            Integer monitorTypeId = monitorItem.getMonitorTypeId();

            Double nowValue = null;
            String nowDetail = null;
            JschUtil jschUtil = new JschUtil(host.getIp(), host.getUserName(), host.getPassword(), host.getPort());
            try {
                jschUtil.testConnect();
                host.setReason("");
                hostMapper.updateById(host);
            }
            catch (BindException   e){
                e.printStackTrace();
            }
            catch (Exception e){
                host.setEnable(0);
                host.setReason("连接服务器失败，请检查！");
                hostMapper.updateById(host);
                e.printStackTrace();
                continue;
            }
           
            if (monitorTypeId == 1) {
                Double warnValue = monitorItem.getWarnValue();
                try {
                    nowValue = Double.valueOf(jschUtil.getCPUUsage());
                } catch (Exception e) {
                    warnValue = -1.0;
                    log.info("连接" + host.getIp() + "或命令执行失败！" + "getCPUUsage");
                    hostMapper.updateById(host);
                    e.printStackTrace();

                }
                log.info("主机-"+host.getIp()+" 当前cpu使用率："+nowValue);
                if (nowValue > warnValue) {
//                  cpu使用率过高告警
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowValue));
                    monitorAlarm.setAlarmTime(formattedDateTime);
                    monitorAlarm.setMonitorItemId(monitorItem.getId());

                    monitorAlarmMapper.insert(monitorAlarm);
                }

            } else if (monitorTypeId == 2) {
                Double warnValue = monitorItem.getWarnValue();
                try {
                    nowValue = Double.valueOf(jschUtil.getMemoryRemain());
                } catch (Exception e) {
                    warnValue = -1.0;
                    hostMapper.updateById(host);
                    log.info("连接" + host.getIp() + "或命令执行失败! " + "getMemoryRemain");
                    e.printStackTrace();

                }
                log.info("主机-"+ host.getIp()+" 当前内存剩余空间（单位:G）："+nowValue);
                if (nowValue < warnValue) {
//                    内存过小告警
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowValue));
                    monitorAlarm.setAlarmTime(formattedDateTime);
                    monitorAlarm.setMonitorItemId(monitorItem.getId());

                    monitorAlarmMapper.insert(monitorAlarm);
                }
            } else if (monitorTypeId == 3) {
                Double warnValue = monitorItem.getWarnValue();
                String detail = monitorItem.getDetail();
                try {
                    nowValue = Double.valueOf(jschUtil.getDiskPartitionUsage(detail));
                } catch (Exception e) {
                    nowValue = -1.0;
                    hostMapper.updateById(host);
                    log.info("连接" + host.getIp() + "或命令执行失败! " + "getDiskPartitionUsage");
                    e.printStackTrace();

                }
                log.info("主机-"+ host.getIp()+" 当前磁盘分区 " + detail +" 剩余空间(单位:G)："+ nowValue);
                if (nowValue < warnValue) {
//                    磁盘分区过小告警
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowValue));
                    monitorAlarm.setAlarmTime(formattedDateTime);
                    monitorAlarm.setMonitorItemId(monitorItem.getId());

                    monitorAlarmMapper.insert(monitorAlarm);
                }
            } else if (monitorTypeId == 4) {
                String warnValue = monitorItem.getDetail();
                try {
                    nowDetail = jschUtil.getPortIsLive(String.valueOf(warnValue)) ? "true" : "false";
                } catch (Exception e) {
                    nowDetail = "false";
                    hostMapper.updateById(host);
                    log.info("连接" + host.getIp() + "或命令执行失败! " + "getPortIsLive");
                    e.printStackTrace();

                }
                log.info("主机-"+ host.getIp()+" 当前端口 " + warnValue + " 是否存在："+ nowDetail);
                if (nowDetail == "false") {
//                    端口不存在警告
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowDetail));
                    monitorAlarm.setAlarmTime(formattedDateTime);
                    monitorAlarm.setMonitorItemId(monitorItem.getId());

                    monitorAlarmMapper.insert(monitorAlarm);
                }
            } else if (monitorTypeId == 5) {
                Double warnValue = monitorItem.getWarnValue();
                try {
                    nowDetail = jschUtil.getProcessIsLive(String.valueOf(monitorItem.getDetail())) ? "true" : "false";
                } catch (Exception e) {
                    nowDetail = "false";
                    hostMapper.updateById(host);
                    log.info("连接" + host.getIp() + "或命令执行失败! " + "getProcessIsLive");
                    e.printStackTrace();

                }
                log.info("主机-"+ host.getIp()+" 当前程序 " + monitorItem.getDetail() + " 是否存在："+ nowDetail);
                if (nowDetail == "false") {
//                    程序不存在警告
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowDetail));
                    monitorAlarm.setAlarmTime(formattedDateTime);
                    monitorAlarm.setMonitorItemId(monitorItem.getId());

                    monitorAlarmMapper.insert(monitorAlarm);
                }
            } else if (monitorTypeId == 6) {
                Double warnValue = monitorItem.getWarnValue();
                try {
                    nowDetail = jschUtil.getUrlIsLive(String.valueOf(monitorItem.getDetail())) ? "true" : "false";
                } catch (Exception e) {
                    nowDetail = "false";
                    hostMapper.updateById(host);
                    log.info("连接" + host.getIp() + "或命令执行失败!" + "getUrlIsLive");
                    e.printStackTrace();

                }
                log.info("主机-"+ host.getIp()+" 当前url " + monitorItem.getDetail() + " 访问是否成功："+ nowDetail);
                if (nowDetail == "false") {
//                    URL访问失败警告
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowDetail));
                    monitorAlarm.setAlarmTime(formattedDateTime);
                    monitorAlarm.setMonitorItemId(monitorItem.getId());

                    monitorAlarmMapper.insert(monitorAlarm);
                }
            }
            jschUtil.connectClose();
//            删除当前一天前的数据
            LocalDate currentDate = LocalDate.now();
            LocalDate oneDayAgo = currentDate.minusDays(1);
            Date oneDayAgoDate = Date.from(oneDayAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
            monitorDataMapper.deleteOldData(oneDayAgoDate);
//            添加监控数据
            monitorData.setData(nowValue != null ? String.valueOf(nowValue) : nowDetail);
            monitorData.setMonitorTypeId(monitorTypeId);
            monitorData.setHostId(hostId);
            monitorData.setMonitorItemId(monitorItem.getId());
            monitorData.setCreateTime(formattedDateTime);
            monitorDataService.addMonitorData(monitorData);

//            返回的监控数据
            MonitorDataResult monitorDataResult = new MonitorDataResult();
            monitorDataResult.setData(nowValue != null ? String.valueOf(nowValue) : nowDetail);
            monitorDataResult.setCreateTime(formattedDateTime);
            monitorDataResult.setHostName(host.getHostName());
            monitorDataResult.setDetail(monitorItem.getDetail());
            monitorDataResult.setMonitorTypeName(monitorTypeMapper.selectById(monitorTypeId).getName());

            monitorDataResults.add(monitorDataResult);
        }
        Result result = new Result<>(200,monitorDataResults,"查询监控数据成功！");
        sendMonitorDataToAllSessions(result);
    }
}
