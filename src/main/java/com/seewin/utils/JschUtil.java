package com.seewin.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

@Slf4j
public class JschUtil {
    private String host;
    private String username;
    private String password;
    private int port;
    private Session session;
    private ChannelExec channel;

    public JschUtil(String host, String username, String password, int port) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public void testConnect() throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);

        // 跳过公钥检查
        session.setConfig("StrictHostKeyChecking", "no");
//        设置超时时间
        session.setTimeout(3000);
        // 开启 SSH 连接
        session.connect();
    }

    public void connectClose() {
        // 关闭连接
//        channel.disconnect();
        session.disconnect();
    }

    public String getDiskPartitionUsage(String diskPartition) throws Exception {


        // 执行命令
        String command = "df -h | awk '{if($6 == \"" + diskPartition + "\") {print $4}}'";
        channel = (ChannelExec) session.openChannel("exec");
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

        }
        String unit = result.replaceAll("[^a-zA-Z]", "");
        Double remian = Double.valueOf(result.replaceAll("[a-zA-Z]", ""));
        DecimalFormat format = new DecimalFormat("#.00");
        if (unit.equals("M")) {
            result = format.format(remian / 1000);
        } else if (unit.equals("T")) {
            result = format.format(remian * 1000);
        } else if (unit.equals("G")) {
            result = format.format(remian);
        }

        return result;

    }

    public String getCPUUsage() throws Exception {


        // 执行命令
        String command = "top -n 1 -b | grep \"%Cpu\" | awk '{print $8}'";
        channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.connect();

        // 读取命令执行结果
        InputStream in = channel.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
//          cpuID表示cpu空闲
        String cpuId = null;
        String result = null;
        while ((line = reader.readLine()) != null) {
//                String [] filed = line.split("\\s+");
            cpuId = line;

        }

//        result = String.valueOf(100.0 - Double.valueOf(cpuId));
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

//        log.info("cpuId: "+cpuId);
        result = decimalFormat.format(100.0 - Double.valueOf(cpuId));
//        log.info("result: "+result);
        return result;

    }

    public String getMemoryRemain() throws Exception {

        // 执行命令
        String command = "free -mh | awk 'NR==2{print $4}'";
        channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.connect();

        // 读取命令执行结果
        InputStream in = channel.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        String memory = null;
        String result = null;
        String unit = null;
        while ((line = reader.readLine()) != null) {
//                String [] filed = line.split("\\s+");
            memory = line;

        }
        unit = memory.replaceAll("[^a-zA-Z]", "");
        Double remian = Double.valueOf(memory.replaceAll("[a-zA-Z]", ""));
        DecimalFormat decimalFormatt = new DecimalFormat("#.00");
        if (unit.equals("M")) {
            result = decimalFormatt.format(remian / 1000);
        } else if (unit.equals("T")) {
            result = decimalFormatt.format(remian * 1000);
        } else if (unit.equals("G")) {
            result = decimalFormatt.format(remian);
        }
        return result;

    }

    public boolean getPortIsLive(String specifyPort) throws Exception {

        // 执行命令
        String command = "ss -nplt | awk '{print $4}' | awk -F ':' '{print $2}' | grep " + specifyPort;
        channel = (ChannelExec) session.openChannel("exec");
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
        }
        if (result != null && !result.isEmpty()) {
            return true;
        }

        return false;
    }

    public boolean getProcessIsLive(String specifyProcess) throws Exception {

        // 执行命令
        String command = "ps -axu | grep " + specifyProcess;
        channel = (ChannelExec) session.openChannel("exec");
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

        }
        if (result != null && !result.isEmpty()) {
            return true;
        }

        return false;
    }

    public boolean getUrlIsLive(String url) throws Exception {
        // 创建一个RestTemplate实例
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.getForEntity(url, String.class);
        }

        catch (Exception e){
            e.printStackTrace();
//            log.info(e.getMessage());
//            log.info(String.valueOf(e.getCause()));
            if (e.getMessage().contains("PKIX path validation failed:")) {
                log.error("PKIX path validation failed: " + e.getMessage());
                return true;
            } else {
                return false;
            }
        }
        // 发送GET请求，并获取响应


        // 检查请求是否成功
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        } else {
            return false;
        }
    }
}
