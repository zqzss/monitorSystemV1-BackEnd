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

    public void testConnect() throws Exception{
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);

        // 跳过公钥检查
        session.setConfig("StrictHostKeyChecking", "no");

        // 开启 SSH 连接
        session.connect();
    }
    public void connectClose(){
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
        if (unit.equals("M")) {
            result = String.valueOf(remian / 1000);
        } else if (unit.equals("T")) {
            result = String.valueOf(remian * 1000);
        } else if (unit.equals("G")) {
            result = String.valueOf(remian);
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
        result = String.valueOf(100.0 - Double.valueOf(cpuId));

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
        if (unit.equals("M")) {
            result = String.valueOf(remian / 1000);
        } else if (unit.equals("T")) {
            result = String.valueOf(remian * 1000);
        } else if (unit.equals("G")) {
            result = String.valueOf(remian);
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

        // 发送GET请求，并获取响应
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // 检查请求是否成功
        if (response.getStatusCode().is2xxSuccessful()) {
            return true;
        } else {
            return false;
        }
    }
}
