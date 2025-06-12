package com.seewin.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jcraft.jsch.JSchException;
import com.seewin.entity.*;
import com.seewin.mapper.*;
import com.seewin.service.EmailService;
import com.seewin.service.MonitorDataService;
import com.seewin.utils.JschUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
@EnableAsync
public class GetMonitorDataRunable{
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

    @Async("taskExecutor")
    public void run(MonitorItem monitorItem) {
        List<MonitorDataResult> monitorDataResults = new ArrayList<>();
//        log.info(" monitorItem2: "+monitorItem.toString());
        Integer monitorItemId = monitorItem.getId();
//          找到监控项对应的主机
        Integer hostId = monitorItem.getHostId();
        Host host = hostMapper.selectById(hostId);
        String hostName = host.getHostName();
        String ip = host.getIp();

        if (host.getEnable() != 1) {
            return;
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
//            更新连接失败原因和剩余重试次数
            if (!"".equals(host.getReason()) || host.getReConnectNumber() != reConnectNumber) {
                host.setReason("");
                host.setReConnectNumber(reConnectNumber);
                hostMapper.updateById(host);
            }

        } catch (JSchException e) {
            e.printStackTrace();
            String message = e.getMessage();
            if (host.getReConnectNumber() > 0) {
                Integer reConnectNumber = host.getReConnectNumber() - 1;
                host.setReConnectNumber(reConnectNumber);

            } else {
                host.setEnable(0);
            }

            if (message.toLowerCase().contains("connection timed out")) {
                host.setReason("连接服务器失败，请检查！");
            } else if (message.toLowerCase().contains("refused")) {
                host.setReason("ssh端口拒绝连接！");
            } else if (message.toLowerCase().contains("auth")) {
                host.setReason("用户或密码错误！");
            } else {
                log.error("！主机：" + hostName + " ip:" + ip + "发送未知异常");
                host.setReason("！主机：" + hostName + " ip:" + ip + "发送未知异常");
            }
            hostMapper.updateById(host);
            log.error("主机连接失败：" + host.toString());
            return;
        } catch (Exception e) {
            if (host.getReConnectNumber() > 0) {
                Integer reConnectNumber = host.getReConnectNumber() - 1;
                host.setReConnectNumber(reConnectNumber);

            } else {
                host.setEnable(0);
            }
            log.error("！主机：" + hostName + " ip:" + ip + "发送未知异常");
            host.setReason("！主机：" + hostName + " ip:" + ip + "发送未知异常");
            hostMapper.updateById(host);
            e.printStackTrace();
            return;
        }
//        cpu使用率
        if (monitorTypeId == 1) {
            Double warnValue = monitorItem.getWarnValue();
            try {
                nowValue = Double.valueOf(jschUtil.getCPUUsage());
            } catch (Exception e) {
                warnValue = -1.0;
                log.error("连接" + host.getIp() + "执行命令失败！" + "getCPUUsage");
                hostMapper.updateById(host);
                e.printStackTrace();

            }
            log.info("主机-" + host.getIp() + " 当前cpu使用率：" + nowValue);
            if (nowValue > warnValue) {
//                  cpu使用率过高告警
//                  存入告警记录
                MonitorAlarm monitorAlarm = new MonitorAlarm();
                monitorAlarm.setHostId(hostId);
                monitorAlarm.setMonitorTypeId(monitorTypeId);
                monitorAlarm.setData(String.valueOf(nowValue));
                monitorAlarm.setAlarmTime(formattedDateTime);
                monitorAlarm.setMonitorItemId(monitorItem.getId());

                monitorAlarmMapper.insert(monitorAlarm);
//                    用通知名称获取通知类型，再获取通知间隔时间
                LambdaQueryWrapper<NoticeType> noticeTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                noticeTypeLambdaQueryWrapper.eq(NoticeType::getName, "邮箱通知");
                NoticeType noticeType = noticeTypeMapper.selectOne(noticeTypeLambdaQueryWrapper);
                Integer intervalMinute = noticeType.getIntervalMinute();
                Integer noticeTypeId = noticeType.getId();
                log.info("noticeType: " + noticeType.toString());
//                    用监控项id和通知类型id找到通知项，并发邮件通知
                LambdaQueryWrapper<NoticeItem> noticeItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                    noticeItemLambdaQueryWrapper.eq(NoticeItem::getMonitorItemId, monitorItemId);
                noticeItemLambdaQueryWrapper.eq(NoticeItem::getNoticeTypeId, noticeTypeId);
                List<NoticeItem> noticeItems = noticeItemMapper.selectList(noticeItemLambdaQueryWrapper);
                for (NoticeItem noticeItem : noticeItems) {

                    LocalDateTime lastNoticeTime = noticeItem.getLastNoticeTime() != null ? noticeItem.getLastNoticeTime() : null;
                    LocalDateTime lastIntervalNoticeTime = LocalDateTime.now();
                    if (lastNoticeTime != null) {
                        lastIntervalNoticeTime = lastNoticeTime.plusMinutes(intervalMinute);
                    }

                    if (now.compareTo(lastIntervalNoticeTime) > 0 || lastNoticeTime == null) {
                        Integer userId = noticeItem.getUserId();
                        User user = userMapper.selectById(userId);
                        String email = user.getEmail();

//                            发邮件通知
                        MonitorItem monitorItem1 = monitorItemMapper.selectById(monitorItemId);
                        Double warnValue1 = monitorItem1.getWarnValue();
                        String message = "主机-" + hostName + " ip:" + ip + " 当前CPU:" + nowValue + " > " + warnValue;
                        emailService.sendEmail(email, "服务器CPU告警", message);
//                            更新最后一次通知时间
                        noticeItem.setLastNoticeTime(now);
                        noticeItemMapper.updateById(noticeItem);
                    }
                }

            }

        }
//        内存
        else if (monitorTypeId == 2) {
            Double warnValue = monitorItem.getWarnValue();
            try {
                nowValue = Double.valueOf(jschUtil.getMemoryRemain());
            } catch (Exception e) {
                warnValue = -1.0;
                hostMapper.updateById(host);
                log.info("连接" + host.getIp() + "执行命令失败! " + "getMemoryRemain");
                e.printStackTrace();

            }
            log.info("主机-" + host.getIp() + " 当前内存剩余空间（单位:G）：" + nowValue);
            if (nowValue < warnValue) {
//                    内存过小告警
                MonitorAlarm monitorAlarm = new MonitorAlarm();
                monitorAlarm.setHostId(hostId);
                monitorAlarm.setMonitorTypeId(monitorTypeId);
                monitorAlarm.setData(String.valueOf(nowValue));
                monitorAlarm.setAlarmTime(formattedDateTime);
                monitorAlarm.setMonitorItemId(monitorItem.getId());

                monitorAlarmMapper.insert(monitorAlarm);

                //                    用通知名称获取通知类型，再获取通知间隔时间
                LambdaQueryWrapper<NoticeType> noticeTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                noticeTypeLambdaQueryWrapper.eq(NoticeType::getName, "邮箱通知");
                NoticeType noticeType = noticeTypeMapper.selectOne(noticeTypeLambdaQueryWrapper);
                Integer intervalMinute = noticeType.getIntervalMinute();
                Integer noticeTypeId = noticeType.getId();
//                    用监控项id和通知类型id找到通知项，并发邮件通知
                LambdaQueryWrapper<NoticeItem> noticeItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                    noticeItemLambdaQueryWrapper.eq(NoticeItem::getMonitorItemId, monitorItemId);
                noticeItemLambdaQueryWrapper.eq(NoticeItem::getNoticeTypeId, noticeTypeId);
                List<NoticeItem> noticeItems = noticeItemMapper.selectList(noticeItemLambdaQueryWrapper);
                for (NoticeItem noticeItem : noticeItems) {
                    LocalDateTime lastNoticeTime = noticeItem.getLastNoticeTime() != null ? noticeItem.getLastNoticeTime() : null;
                    LocalDateTime lastIntervalNoticeTime = LocalDateTime.now();
                    if (lastNoticeTime != null) {
                        lastIntervalNoticeTime = lastNoticeTime.plusMinutes(intervalMinute);
                    }

                    if (now.compareTo(lastIntervalNoticeTime) > 0 || lastNoticeTime == null) {
                        Integer userId = noticeItem.getUserId();
                        User user = userMapper.selectById(userId);
                        String email = user.getEmail();

//                            发邮件通知
                        MonitorItem monitorItem1 = monitorItemMapper.selectById(monitorItemId);
                        Double warnValue1 = monitorItem1.getWarnValue();
                        String message = "主机-" + hostName + " ip:" + ip + " 当前内存:" + nowValue + " < " + warnValue;
                        emailService.sendEmail(email, "服务器内存告警", message);
//                            更新最后一次通知时间
                        noticeItem.setLastNoticeTime(now);
                        noticeItemMapper.updateById(noticeItem);
                    }
                }
            }
        } else if (monitorTypeId == 3) {
            Double warnValue = monitorItem.getWarnValue();
            String detail = monitorItem.getDetail();
            try {
                nowValue = Double.valueOf(jschUtil.getDiskPartitionUsage(detail));
            } catch (Exception e) {
                nowValue = -1.0;
                hostMapper.updateById(host);
                log.info("连接" + host.getIp() + "执行命令失败! " + "getDiskPartitionUsage");
                e.printStackTrace();

            }
            log.info("主机-" + host.getIp() + " 当前磁盘分区 " + detail + " 剩余空间(单位:G)：" + nowValue);
            if (nowValue < warnValue) {
//                    磁盘分区过小告警
                MonitorAlarm monitorAlarm = new MonitorAlarm();
                monitorAlarm.setHostId(hostId);
                monitorAlarm.setMonitorTypeId(monitorTypeId);
                monitorAlarm.setData(String.valueOf(nowValue));
                monitorAlarm.setAlarmTime(formattedDateTime);
                monitorAlarm.setMonitorItemId(monitorItem.getId());

                monitorAlarmMapper.insert(monitorAlarm);
                //                    用通知名称获取通知类型，再获取通知间隔时间
                LambdaQueryWrapper<NoticeType> noticeTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                noticeTypeLambdaQueryWrapper.eq(NoticeType::getName, "邮箱通知");
                NoticeType noticeType = noticeTypeMapper.selectOne(noticeTypeLambdaQueryWrapper);
                Integer intervalMinute = noticeType.getIntervalMinute();
                Integer noticeTypeId = noticeType.getId();
//                    用监控项id和通知类型id找到通知项，并发邮件通知
                LambdaQueryWrapper<NoticeItem> noticeItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                    noticeItemLambdaQueryWrapper.eq(NoticeItem::getMonitorItemId, monitorItemId);
                noticeItemLambdaQueryWrapper.eq(NoticeItem::getNoticeTypeId, noticeTypeId);
                List<NoticeItem> noticeItems = noticeItemMapper.selectList(noticeItemLambdaQueryWrapper);
                for (NoticeItem noticeItem : noticeItems) {
                    LocalDateTime lastNoticeTime = noticeItem.getLastNoticeTime() != null ? noticeItem.getLastNoticeTime() : null;
                    LocalDateTime lastIntervalNoticeTime = LocalDateTime.now();
                    if (lastNoticeTime != null) {
                        lastIntervalNoticeTime = lastNoticeTime.plusMinutes(intervalMinute);
                    }
                    if (now.compareTo(lastIntervalNoticeTime) > 0 || lastNoticeTime == null) {
                        Integer userId = noticeItem.getUserId();
                        User user = userMapper.selectById(userId);
                        String email = user.getEmail();

//                            发邮件通知
                        MonitorItem monitorItem1 = monitorItemMapper.selectById(monitorItemId);
                        Double warnValue1 = monitorItem1.getWarnValue();
                        String message = "主机-" + hostName + " ip:" + ip + " 当前磁盘分区:" + detail + " 剩余空间：" + nowValue + "G " + " < " + warnValue + "G ";
                        emailService.sendEmail(email, "服务器磁盘分区告警", message);
//                            更新最后一次通知时间
                        noticeItem.setLastNoticeTime(now);
                        noticeItemMapper.updateById(noticeItem);
                    }
                }
            }
        } else if (monitorTypeId == 4) {
            String detail = monitorItem.getDetail();
            try {
                nowDetail = jschUtil.getPortIsLive(String.valueOf(detail)) ? "true" : "false";
            } catch (Exception e) {
                nowDetail = "false";
                hostMapper.updateById(host);
                log.info("连接" + host.getIp() + "执行命令失败! " + "getPortIsLive");
                e.printStackTrace();

            }
            log.info("主机-" + host.getIp() + " 当前端口 " + detail + " 是否存在：" + nowDetail);
            if (nowDetail == "false") {
//                    端口不存在警告
                MonitorAlarm monitorAlarm = new MonitorAlarm();
                monitorAlarm.setHostId(hostId);
                monitorAlarm.setMonitorTypeId(monitorTypeId);
                monitorAlarm.setData(String.valueOf(nowDetail));
                monitorAlarm.setAlarmTime(formattedDateTime);
                monitorAlarm.setMonitorItemId(monitorItem.getId());

                monitorAlarmMapper.insert(monitorAlarm);
                //                    用通知名称获取通知类型，再获取通知间隔时间
                LambdaQueryWrapper<NoticeType> noticeTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                noticeTypeLambdaQueryWrapper.eq(NoticeType::getName, "邮箱通知");
                NoticeType noticeType = noticeTypeMapper.selectOne(noticeTypeLambdaQueryWrapper);
                Integer intervalMinute = noticeType.getIntervalMinute();
                Integer noticeTypeId = noticeType.getId();
//                    用通知类型id找到通知项，并发邮件通知
                LambdaQueryWrapper<NoticeItem> noticeItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                    noticeItemLambdaQueryWrapper.eq(NoticeItem::getMonitorItemId, monitorItemId);
                noticeItemLambdaQueryWrapper.eq(NoticeItem::getNoticeTypeId, noticeTypeId);
                List<NoticeItem> noticeItems = noticeItemMapper.selectList(noticeItemLambdaQueryWrapper);
                for (NoticeItem noticeItem : noticeItems) {
                    LocalDateTime lastNoticeTime = noticeItem.getLastNoticeTime() != null ? noticeItem.getLastNoticeTime() : null;
                    LocalDateTime lastIntervalNoticeTime = LocalDateTime.now();
                    if (lastNoticeTime != null) {
                        lastIntervalNoticeTime = lastNoticeTime.plusMinutes(intervalMinute);
                    }
                    if (now.compareTo(lastIntervalNoticeTime) > 0 || lastNoticeTime == null) {
                        Integer userId = noticeItem.getUserId();
                        User user = userMapper.selectById(userId);
                        String email = user.getEmail();

//                            发邮件通知
                        MonitorItem monitorItem1 = monitorItemMapper.selectById(monitorItemId);
                        Double warnValue1 = monitorItem1.getWarnValue();
                        String message = "主机-" + hostName + " ip:" + ip + " 当前端口: " + detail + "不存在";
                        emailService.sendEmail(email, "服务器端口告警", message);
//                            更新最后一次通知时间
                        noticeItem.setLastNoticeTime(now);
                        noticeItemMapper.updateById(noticeItem);
                    }
                }
            }
        }
//            进程名称
        else if (monitorTypeId == 5) {
            Double warnValue = monitorItem.getWarnValue();
            String detail = monitorItem.getDetail();
            try {
                nowDetail = jschUtil.getProcessIsLive(String.valueOf(detail)) ? "true" : "false";
            } catch (Exception e) {
                nowDetail = "false";
                hostMapper.updateById(host);
                log.info("连接" + host.getIp() + "执行命令失败! " + "getProcessIsLive");
                e.printStackTrace();

            }
            log.info("主机-" + host.getIp() + " 当前程序 " + monitorItem.getDetail() + " 是否存在：" + nowDetail);
            if (nowDetail == "false") {
//                    程序不存在警告
                MonitorAlarm monitorAlarm = new MonitorAlarm();
                monitorAlarm.setHostId(hostId);
                monitorAlarm.setMonitorTypeId(monitorTypeId);
                monitorAlarm.setData(String.valueOf(nowDetail));
                monitorAlarm.setAlarmTime(formattedDateTime);
                monitorAlarm.setMonitorItemId(monitorItem.getId());

                monitorAlarmMapper.insert(monitorAlarm);

                //                    用通知名称获取通知类型，再获取通知间隔时间
                LambdaQueryWrapper<NoticeType> noticeTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                noticeTypeLambdaQueryWrapper.eq(NoticeType::getName, "邮箱通知");
                NoticeType noticeType = noticeTypeMapper.selectOne(noticeTypeLambdaQueryWrapper);
                Integer intervalMinute = noticeType.getIntervalMinute();
                Integer noticeTypeId = noticeType.getId();
//                    用通知类型id找到通知项，并发邮件通知
                LambdaQueryWrapper<NoticeItem> noticeItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                    noticeItemLambdaQueryWrapper.eq(NoticeItem::getMonitorItemId, monitorItemId);
                noticeItemLambdaQueryWrapper.eq(NoticeItem::getNoticeTypeId, noticeTypeId);
                List<NoticeItem> noticeItems = noticeItemMapper.selectList(noticeItemLambdaQueryWrapper);
                for (NoticeItem noticeItem : noticeItems) {
                    LocalDateTime lastNoticeTime = noticeItem.getLastNoticeTime() != null ? noticeItem.getLastNoticeTime() : null;
                    LocalDateTime lastIntervalNoticeTime = LocalDateTime.now();
                    if (lastNoticeTime != null) {
                        lastIntervalNoticeTime = lastNoticeTime.plusMinutes(intervalMinute);
                    }
                    if (now.compareTo(lastIntervalNoticeTime) > 0 || lastNoticeTime == null) {
                        Integer userId = noticeItem.getUserId();
                        User user = userMapper.selectById(userId);
                        String email = user.getEmail();

//                            发邮件通知
                        MonitorItem monitorItem1 = monitorItemMapper.selectById(monitorItemId);
                        Double warnValue1 = monitorItem1.getWarnValue();
                        String message = "主机-" + hostName + " ip:" + ip + " 当前进程: " + detail + "不存在";
                        emailService.sendEmail(email, "服务器进程告警", message);
//                            更新最后一次通知时间
                        noticeItem.setLastNoticeTime(now);
                        noticeItemMapper.updateById(noticeItem);
                    }
                }
            }
        }
//                url
        else if (monitorTypeId == 6) {
            Double warnValue = monitorItem.getWarnValue();
            String detail = monitorItem.getDetail();
            try {
                nowDetail = jschUtil.getUrlIsLive(String.valueOf(detail)) ? "true" : "false";
            } catch (Exception e) {
                nowDetail = "false";
                hostMapper.updateById(host);
                log.info("连接" + host.getIp() + "执行命令失败!" + "getUrlIsLive");
                e.printStackTrace();

            }
            log.info("主机-" + host.getIp() + " 当前url " + monitorItem.getDetail() + " 访问是否成功：" + nowDetail);
            if (nowDetail == "false") {
//                    URL访问失败警告
                MonitorAlarm monitorAlarm = new MonitorAlarm();
                monitorAlarm.setHostId(hostId);
                monitorAlarm.setMonitorTypeId(monitorTypeId);
                monitorAlarm.setData(String.valueOf(nowDetail));
                monitorAlarm.setAlarmTime(formattedDateTime);
                monitorAlarm.setMonitorItemId(monitorItem.getId());

                monitorAlarmMapper.insert(monitorAlarm);

                //                    用通知名称获取通知类型，再获取通知间隔时间
                LambdaQueryWrapper<NoticeType> noticeTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                noticeTypeLambdaQueryWrapper.eq(NoticeType::getName, "邮箱通知");
                NoticeType noticeType = noticeTypeMapper.selectOne(noticeTypeLambdaQueryWrapper);
                Integer intervalMinute = noticeType.getIntervalMinute();
                Integer noticeTypeId = noticeType.getId();
//                    用通知类型id找到通知项，并发邮件通知
                LambdaQueryWrapper<NoticeItem> noticeItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                    noticeItemLambdaQueryWrapper.eq(NoticeItem::getMonitorItemId, monitorItemId);
                noticeItemLambdaQueryWrapper.eq(NoticeItem::getNoticeTypeId, noticeTypeId);
                List<NoticeItem> noticeItems = noticeItemMapper.selectList(noticeItemLambdaQueryWrapper);
                for (NoticeItem noticeItem : noticeItems) {
                    LocalDateTime lastNoticeTime = noticeItem.getLastNoticeTime() != null ? noticeItem.getLastNoticeTime() : null;
                    LocalDateTime lastIntervalNoticeTime = LocalDateTime.now();
                    if (lastNoticeTime != null) {
                        lastIntervalNoticeTime = lastNoticeTime.plusMinutes(intervalMinute);
                    }
                    if (now.compareTo(lastIntervalNoticeTime) > 0 || lastNoticeTime == null) {
                        Integer userId = noticeItem.getUserId();
                        User user = userMapper.selectById(userId);
                        String email = user.getEmail();

//                            发邮件通知
                        MonitorItem monitorItem1 = monitorItemMapper.selectById(monitorItemId);
                        Double warnValue1 = monitorItem1.getWarnValue();
                        String message = "主机-" + hostName + " ip:" + ip + " 当前url: " + detail + "不存在";
                        emailService.sendEmail(email, "服务器进程告警", message);
//                            更新最后一次通知时间
                        noticeItem.setLastNoticeTime(now);
                        noticeItemMapper.updateById(noticeItem);

                    }
                }
            }
        }
        jschUtil.connectClose();

//            添加监控数据
        MonitorData monitorData = new MonitorData();
        monitorData.setData(nowValue != null ? String.valueOf(nowValue) : nowDetail);
        monitorData.setMonitorTypeId(monitorTypeId);
        monitorData.setHostId(hostId);
        monitorData.setMonitorItemId(monitorItemId);
        monitorData.setCreateTime(formattedDateTime);
        monitorDataMapper.insert(monitorData);

//            返回的监控数据
        MonitorDataResult monitorDataResult = new MonitorDataResult();
        monitorDataResult.setData(nowValue != null ? String.valueOf(nowValue) : nowDetail);
        monitorDataResult.setCreateTime(formattedDateTime);
        monitorDataResult.setHostName(host.getHostName());
        monitorDataResult.setDetail(monitorItem.getDetail());
        monitorDataResult.setMonitorTypeName(monitorTypeMapper.selectById(monitorTypeId).getName());

        monitorDataResults.add(monitorDataResult);

        Result result = new Result<>(200, monitorDataResults, "查询监控数据成功！");
//        sendMonitorDataToAllSessions(result);


    }
}
