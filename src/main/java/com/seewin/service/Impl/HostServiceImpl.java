package com.seewin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcraft.jsch.JSchException;
import com.seewin.entity.Host;
import com.seewin.entity.Result;
import com.seewin.mapper.HostMapper;
import com.seewin.service.HostService;
import com.seewin.utils.JschUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class HostServiceImpl implements HostService {
    @Autowired
    private HostMapper hostMapper;

    @Value("${reConnectNumber}")
    private Integer reConnectNumber;

    @Override
    public Result addHost(Host host) {
        log.info("添加主机-"+ host.toString());

//      获取host类的每个属性，
        Field[] declaredFields = host.getClass().getDeclaredFields();
        for(Field field : declaredFields){
            field.setAccessible(true);
            try {
                Object value = field.get(host); // 获取字段的值
                if (field.getName().equals("description")||field.getName().equals("id")||field.getName().equals("deleted")||field.getName().equals("version")||field.getName().equals("reason")||field.getName().equals("reConnectNumber")||field.getName().equals("enable")){
                    continue;
                }
                if (value == null||"".equals(value)) {
                    
                    return new Result<>(500,null,"添加失败！"+value.toString()+"不能为空");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        JschUtil jschUtil =new JschUtil(host.getIp(), host.getUserName(), host.getPassword(), host.getPort());
        try {
            jschUtil.testConnect();
            host.setEnable(1);
            if (!"".equals(host.getReason()) || host.getReConnectNumber() != reConnectNumber) {
                host.setReason("");
                host.setReConnectNumber(reConnectNumber);
            }

        } catch (JSchException e) {
            e.printStackTrace();
            String message = e.getMessage();
            host.setEnable(0);
            host.setReConnectNumber(0);
            if (message.toLowerCase().contains("connection timed out")){
                host.setReason("连接服务器失败，请检查！");
            }
            else if (message.toLowerCase().contains("refused")){
                host.setReason("ssh端口拒绝连接！");
            }
            else if (message.toLowerCase().contains("auth")){
                host.setReason("用户或密码错误！");
            }
            else {
                log.error("新添主机："+host.getHostName() +" ip:"+host.getIp() + "发送未知异常");
                host.setReason("新添主机："+host.getHostName() + " ip:"+host.getIp() + "发送未知异常");
            }


        } catch (Exception e) {

            host.setEnable(0);
            host.setReConnectNumber(0);
            log.error("新添主机："+host.getHostName()+" ip:"+host.getIp() + "发送未知异常");
            host.setReason("新添主机："+host.getHostName()+" ip:"+host.getIp() + "发送未知异常");
            e.printStackTrace();

        }
        hostMapper.insert(host);
        return new Result<>(200,null,"添加主机成功！");
    }

    @Override
    public Result<List<Host>> getHostAll(HashMap queryData) {
        String hostName = queryData.get("inputHostName")!=null?queryData.get("inputHostName").toString():null;

        Integer currentPage = queryData.get("currentPage")!=null? Integer.valueOf((String) queryData.get("currentPage")) :null;
        Integer pageSize = queryData.get("pageSize")!=null? Integer.valueOf((String) queryData.get("pageSize")) :null;
        LambdaQueryWrapper<Host> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(hostName!=null,Host::getHostName,hostName);

        IPage<Host> page = new Page(currentPage,pageSize);
        IPage<Host> hostIPage = hostMapper.selectPage(page, lambdaQueryWrapper);
        List<Host> records = hostIPage.getRecords();
        Integer total = Math.toIntExact(hostIPage.getTotal());

        HashMap resultMap = new HashMap<>();
        resultMap.put("tableData",records);
        resultMap.put("total",total);
        log.info("分页查询主机条件：【" + "inputHostName" + hostName + ", currentPage: "+currentPage+", pageSize: "+page +"】");
        log.info("分页查询主机返回结果："+resultMap);
        return new Result(200,resultMap,"查询成功！");
    }

    @Override
    public Result updateHost(Host host) {
        host.setEnable(1);
        String hostName = host.getHostName();
        String ip = host.getIp();
        String password = host.getPassword();
        String username = host.getUserName();
        int port = host.getPort();
        JschUtil jschUtil =new JschUtil(ip, username, password, port);
        try {
            jschUtil.testConnect();
            if (!"".equals(host.getReason()) || host.getReConnectNumber() != reConnectNumber) {
                host.setReason("");
                host.setReConnectNumber(reConnectNumber);

            }

        } catch (JSchException e) {
            e.printStackTrace();
            String message = e.getMessage();

            host.setEnable(0);
            host.setReConnectNumber(0);
            if (message.toLowerCase().contains("time")){
                host.setReason("连接服务器失败，请检查！");
            }
            else if (message.toLowerCase().contains("refused")){
                host.setReason("ssh端口拒绝连接！");
            }
            else if (message.toLowerCase().contains("auth")){
                host.setReason("用户或密码错误！");
            }
            else {
                log.error("！主机："+hostName+" ip:"+ip + "发送未知异常");
                host.setReason("！主机："+hostName+" ip:"+ip + "发送未知异常");
            }


        } catch (Exception e) {

            host.setEnable(0);
            host.setReConnectNumber(0);
            log.error("！主机："+hostName+" ip:"+ip + "发送未知异常");
            host.setReason("！主机："+hostName+" ip:"+ip + "发送未知异常");
            e.printStackTrace();

        }
        hostMapper.updateById(host);
        log.info("更新主机后："+host.toString());
        return new Result<>(200,null,"修改成功！");
    }

    @Override
    public Result deleteHost( Integer id) {
        Host host = hostMapper.selectById(id);
        hostMapper.deleteById(id);
        log.info("删除主机："+host);
        return new Result<>(200,null,"删除成功！");
    }

    @Override
    public void fetchHostData() {

    }

    @Override
    public Result<Host> getHostById(Integer id) {
        Host host = hostMapper.selectById(id);
        log.info("根据hostId获取主机: "+host.toString());
        return new Result<>(200,host,"查询成功！");
    }

    @Override
    public Result<List<String>> getHostName() {
        List<Host> hosts = hostMapper.selectList(null);
        List<String> hostName = new ArrayList<>();
        for(Host host:hosts){
            hostName.add(host.getHostName());
        }
        log.info("返回所有主机的名称: "+hostName);
        return new Result<>(200,hostName,"查询成功！");
    }
}
