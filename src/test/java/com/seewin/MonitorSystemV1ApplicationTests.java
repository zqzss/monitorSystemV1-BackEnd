package com.seewin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.seewin.entity.*;
import com.seewin.mapper.*;
import com.seewin.service.EmailService;
import com.seewin.service.MonitorDataService;
import com.seewin.utils.JschUtil;
import com.seewin.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@SpringBootTest
class MonitorSystemV1ApplicationTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MonitorItemMapper monitorItemMapper;
    @Autowired
    private EmailService emailService;
    @Autowired
    private MonitorAlarmMapper monitorAlarmMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private MonitorDataService monitorDataService;
    @Autowired
    private MonitorDataMapper monitorDataMapper;
    @Autowired
    NoticeTypeMapper noticeTypeMapper;
    @Test
    public void testTmp(){
        int cpuCores = Runtime.getRuntime().availableProcessors();
        System.out.println("CPU核心数：" + cpuCores);
    }
    @Test
    public void cpuUsageFormat() throws Exception {
        JschUtil jschUtil = new JschUtil("192.168.1.49","root","sykj@2023",22);
        jschUtil.testConnect();
        String cpuUsage = jschUtil.getCPUUsage();
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String cpuUsageFormat = decimalFormat.format(Double.parseDouble(cpuUsage));
        System.out.println(cpuUsage);
        System.out.println(cpuUsageFormat);
        String result = String.valueOf(100.0 - Double.valueOf(cpuUsageFormat));
        System.out.println(result);
    }

    @Test
    public void doubleFormat() {
        double num = 3.1415926;
        DecimalFormat df = new DecimalFormat("#.00");
        String result = df.format(num);
        System.out.println(result);
    }

    @Test
    public void noticeType() {
        List<Host> hosts = hostMapper.selectList(null);
        log.info(hosts.toString());
        List<NoticeType> noticeTypes = noticeTypeMapper.selectList(null);
        log.info(noticeTypes.get(0).toString());
    }

    @Test
    public void testMD5() {
        String admin = MD5Utils.inputPassToDBPass("admin", "sy");
        System.out.println(admin);
    }

    @Test
    public void testMonitorAlarmRecord() {
        List<MonitorAlarm> monitorAlarms = monitorAlarmMapper.selectMonitorAlarmByQuery(null, null, null, 10, 10, "12:11:11", "13:14:11", null, null);
        System.out.println(monitorAlarms.toString());

    }

    @Test
    void contextLoads() {
        List<MonitorItem> monitorItems = monitorItemMapper.selectList(null);
        for (MonitorItem monitorItem : monitorItems) {
            MonitorData monitorData = new MonitorData();

            Integer hostId = monitorItem.getHostId();
            Host host = hostMapper.selectById(hostId);
//          生成当前时间
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);

            if (host.getEnable() != 1) {
                continue;
            }

            Integer monitorTypeId = monitorItem.getMonitorTypeId();
            Double nowValue = null;
            String nowDetail = null;
            JschUtil jschUtil = new JschUtil(host.getIp(), host.getUserName(), host.getPassword(), host.getPort());
            if (monitorTypeId == 1) {
                Double warnValue = monitorItem.getWarnValue();
                try {
                    nowValue = Double.valueOf(jschUtil.getCPUUsage());
                } catch (Exception e) {
                    host.setEnable(0);
                    log.info("连接" + host.getIp() + "或命令执行失败！");
                    hostMapper.updateById(host);
                    continue;
                }
                if (nowValue > warnValue) {
//                  cpu使用率过高告警
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowValue));
                    monitorAlarm.setAlarmTime(formattedDateTime);

                    monitorAlarmMapper.insert(monitorAlarm);
                }

            } else if (monitorTypeId == 2) {
                Double warnValue = monitorItem.getWarnValue();
                try {
                    nowValue = Double.valueOf(jschUtil.getMemoryRemain());
                } catch (Exception e) {
                    host.setEnable(0);
                    hostMapper.updateById(host);
                    log.info("连接" + host.getIp() + "或命令执行失败！");
                    continue;
                }
                if (nowValue < warnValue) {
//                    内存过小告警
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowValue));
                    monitorAlarm.setAlarmTime(formattedDateTime);

                    monitorAlarmMapper.insert(monitorAlarm);
                }
            } else if (monitorTypeId == 3) {
                Double warnValue = monitorItem.getWarnValue();
                String detail = monitorItem.getDetail();
                try {
                    nowValue = Double.valueOf(jschUtil.getDiskPartitionUsage(detail));
                } catch (Exception e) {
                    host.setEnable(0);
                    hostMapper.updateById(host);
                    log.info("连接" + host.getIp() + "或命令执行失败！");
                    continue;
                }
                if (nowValue < warnValue) {
//                    磁盘分区过小告警
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowValue));
                    monitorAlarm.setAlarmTime(formattedDateTime);

                    monitorAlarmMapper.insert(monitorAlarm);
                }
            } else if (monitorTypeId == 4) {
                Double warnValue = monitorItem.getWarnValue();
                try {
                    nowDetail = jschUtil.getPortIsLive(String.valueOf(monitorItem.getDetail())) ? "true" : "false";
                } catch (Exception e) {
                    host.setEnable(0);
                    hostMapper.updateById(host);
                    log.info("连接" + host.getIp() + "或命令执行失败！");
                    continue;
                }
                if (nowDetail == "false") {
//                    端口不存在警告
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowDetail));
                    monitorAlarm.setAlarmTime(formattedDateTime);

                    monitorAlarmMapper.insert(monitorAlarm);
                }
            } else if (monitorTypeId == 5) {
                Double warnValue = monitorItem.getWarnValue();
                try {
                    nowDetail = jschUtil.getProcessIsLive(String.valueOf(monitorItem.getDetail())) ? "true" : "false";
                } catch (Exception e) {
                    host.setEnable(0);
                    hostMapper.updateById(host);
                    log.info("连接" + host.getIp() + "或命令执行失败！");
                    continue;
                }
                if (nowDetail == "false") {
//                    程序不存在警告
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowDetail));
                    monitorAlarm.setAlarmTime(formattedDateTime);

                    monitorAlarmMapper.insert(monitorAlarm);
                }
            } else if (monitorTypeId == 6) {
                Double warnValue = monitorItem.getWarnValue();
                try {
                    nowDetail = jschUtil.getUrlIsLive(String.valueOf(monitorItem.getDetail())) ? "true" : "false";
                } catch (Exception e) {
                    host.setEnable(0);
                    hostMapper.updateById(host);
                    log.info("连接" + host.getIp() + "或命令执行失败！");
                    continue;
                }
                if (nowDetail == "false") {
//                    URL访问失败警告
                    MonitorAlarm monitorAlarm = new MonitorAlarm();
                    monitorAlarm.setHostId(hostId);
                    monitorAlarm.setMonitorTypeId(monitorTypeId);
                    monitorAlarm.setData(String.valueOf(nowDetail));
                    monitorAlarm.setAlarmTime(formattedDateTime);

                    monitorAlarmMapper.insert(monitorAlarm);
                }
            }


            monitorData.setData(nowValue != null ? String.valueOf(nowValue) : nowDetail);
            monitorData.setMonitorTypeId(monitorTypeId);
            monitorData.setHostId(hostId);
            monitorData.setCreateTime(formattedDateTime);
            log.info(monitorData.toString());
            monitorDataService.addMonitorData(monitorData);
        }
    }

    @Test
    public void testSelectPage() {
        IPage iPage = new Page(1, 3);
        monitorItemMapper.selectPage(iPage, null);
        System.out.println(iPage.getRecords());
    }

    @Test
    public void restTemplate() {
        // 创建一个RestTemplate实例
        RestTemplate restTemplate = new RestTemplate();

        // 发送GET请求，并获取响应
        ResponseEntity<String> response = restTemplate.getForEntity("https://www.baidu.com", String.class);
        System.out.println(response.getStatusCodeValue());

        // 检查请求是否成功
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("访问成功");
        } else {
            System.out.println("访问失败");
        }
    }

    @Test
    public void sendEmail() {
        String to = "448933144@qq.com";
        String subject = "Test Email";
        String text = "This is a test email.";

        emailService.sendEmail(to, subject, text);
    }

    @Test
    public void testMonitorItem() {
        List<MonitorItem> monitorItems = monitorItemMapper.selectList(null);
        for (MonitorItem monitorItem : monitorItems) {
            System.out.println(monitorItem.toString());
        }
    }

    @Test
    public void test1() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

    }

    @Test
    public void testJSch() {
        String host = "192.168.1.129";
        String username = "root";
        String password = "sykj@20231";
        int port = 22;
        String specifyPort = "22";
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);

            // 跳过公钥检查
            session.setConfig("StrictHostKeyChecking", "no");

            // 开启 SSH 连接
            session.connect();

            // 执行命令
            String command = "ss -nplt | awk '{print $4}' | awk -F ':' '{print $2}' | grep " + specifyPort;
            System.out.println(command);
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.connect();

            // 读取命令执行结果
            InputStream in = channel.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            String result = null;
            while ((line = reader.readLine()) != null) {
//                String [] filed = line.split("\\s+");
                result = line;
                System.out.println(line);
            }
            System.out.println(result);
            if (result != null && !result.equals("")) {
                System.out.println("yes");
                ;
            }
            // 关闭连接
            channel.disconnect();
            session.disconnect();

        } catch (ConnectException e) {
            log.info("111");
        } catch (JSchException e) {
            e.printStackTrace();
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("no");
    }
}
