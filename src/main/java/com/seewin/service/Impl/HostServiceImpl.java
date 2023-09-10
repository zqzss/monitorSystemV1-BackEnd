package com.seewin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seewin.entity.Host;
import com.seewin.entity.Result;
import com.seewin.mapper.HostMapper;
import com.seewin.service.HostService;
import com.seewin.utils.JschUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public Result addHost(Host host) {
        host.setEnable(1);
        log.info("添加主机-"+ host.toString());

//      获取host类的每个属性，
        Field[] declaredFields = host.getClass().getDeclaredFields();
        for(Field field : declaredFields){
            field.setAccessible(true);
            try {
                Object value = field.get(host); // 获取字段的值
                if (field.getName().equals("description")||field.getName().equals("id")||field.getName().equals("deleted")||field.getName().equals("version")||field.getName().equals("reason")){
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
            host.setReason(null);
        }
        catch (Exception e){
            host.setEnable(0);
            host.setReason("连接服务器失败，请检查！");
            log.error("主机-" + host.toString() + "连接服务器失败，请检查！");
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
        return new Result(200,resultMap,"查询成功！");
    }

    @Override
    public Result updateHost(Host host) {
        host.setEnable(1);
        JschUtil jschUtil =new JschUtil(host.getIp(), host.getUserName(), host.getPassword(), host.getPort());
        try {
            jschUtil.testConnect();
            host.setReason(null);
        }
        catch (Exception e){
            host.setEnable(0);
            host.setReason("连接服务器失败，请检查！");
            log.error("主机-" + host.toString() + "连接服务器失败，请检查！");
        }
        hostMapper.updateById(host);
        return new Result<>(200,null,"修改成功！");
    }

    @Override
    public Result deleteHost( Integer id) {
        hostMapper.deleteById(id);
        return new Result<>(200,null,"删除成功！");
    }

    @Override
    public void fetchHostData() {

    }

    @Override
    public Result<Host> getHostById(Integer id) {
        Host host = hostMapper.selectById(id);
        return new Result<>(200,host,"查询成功！");
    }

    @Override
    public Result<List<String>> getHostName() {
        List<Host> hosts = hostMapper.selectList(null);
        List<String> hostName = new ArrayList<>();
        for(Host host:hosts){
            hostName.add(host.getHostName());
        }
        return new Result<>(200,hostName,"查询成功！");
    }
}
