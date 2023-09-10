package com.seewin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seewin.entity.Host;
import com.seewin.entity.MonitorItem;
import com.seewin.entity.MonitorType;
import com.seewin.entity.Result;
import com.seewin.mapper.HostMapper;
import com.seewin.mapper.MonitorItemMapper;
import com.seewin.mapper.MonitorTypeMapper;
import com.seewin.service.MonitorItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MonitorItemServiceImpl implements MonitorItemService {
    @Autowired
    private MonitorItemMapper monitorItemMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private MonitorTypeMapper monitorTypeMapper;
    @Override
    public Result addMonitorItem(Map<String,Object> requestData) {
        MonitorItem monitorItem = new MonitorItem();

        LambdaQueryWrapper<Host> lambdaHostQueryWrapper = new LambdaQueryWrapper<>();
        lambdaHostQueryWrapper.eq(requestData.get("selectHost")!=null,Host::getHostName,requestData.get("selectHost"));
        Host host = hostMapper.selectOne(lambdaHostQueryWrapper);

        LambdaQueryWrapper<MonitorType> lambdaMonitorTypeQueryWrapper = new LambdaQueryWrapper<>();
        lambdaMonitorTypeQueryWrapper.eq(requestData.get("selectMonitorType")!=null,MonitorType::getName,requestData.get("selectMonitorType"));
        MonitorType monitorType = monitorTypeMapper.selectOne(lambdaMonitorTypeQueryWrapper);

        Double warnValue = null;
        if (!requestData.get("warnValue").equals("")){
            warnValue = Double.valueOf((String) requestData.get("warnValue"));
        }
        monitorItem.setHostId(host.getId());
        monitorItem.setMonitorTypeId(monitorType.getId());
        monitorItem.setWarnValue(warnValue);
        monitorItem.setDetail((String) requestData.get("detail"));

        monitorItemMapper.insert(monitorItem);
        return new Result<>(200,null,"新添监控项成功！");
    }

    @Override
    public Result getAllMonitorItem(HashMap queryData) {

        String hostName = queryData.get("inputHostName")!=null?queryData.get("inputHostName").toString():null;

        Integer currentPage = queryData.get("currentPage")!=null? Integer.valueOf((String) queryData.get("currentPage")) :null;
        Integer pageSize = queryData.get("pageSize")!=null? Integer.valueOf((String) queryData.get("pageSize")) :null;
        LambdaQueryWrapper<Host> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(hostName!=null,Host::getHostName,hostName);
        List<Host> hosts = hostMapper.selectList(lambdaQueryWrapper);
        ArrayList host_ids = new ArrayList<>();
        for (Host host : hosts){
            host_ids.add(host.getId());
        }

        LambdaQueryWrapper<MonitorItem> monitorItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        monitorItemLambdaQueryWrapper.in(MonitorItem::getHostId,host_ids);

        IPage<MonitorItem> page = new Page(currentPage,pageSize);

        IPage<MonitorItem> monitorItemIPage = monitorItemMapper.selectPage(page, monitorItemLambdaQueryWrapper);
        List<MonitorItem> monitorItems = monitorItemIPage.getRecords();
        Integer total = Math.toIntExact(monitorItemIPage.getTotal());

        List<Map<String,Object>> responseData = new ArrayList<>();
        HashMap resultData = new HashMap<>();
        resultData.put("total",total);

        for (MonitorItem monitorItem : monitorItems){
            Map<String,Object>  responseMonitorItem = new HashMap<>();

            Integer monitorTypeId = monitorItem.getMonitorTypeId();
            MonitorType monitorType = monitorTypeMapper.selectById(monitorTypeId);

            Integer hostId = monitorItem.getHostId();
            Host host = hostMapper.selectById(hostId);

            responseMonitorItem.put("id",monitorItem.getId());
            responseMonitorItem.put("monitorType",monitorType.getName());
            responseMonitorItem.put("hostName",host.getHostName());
            responseMonitorItem.put("warnValue",monitorItem.getWarnValue());
            responseMonitorItem.put("detail",monitorItem.getDetail());
            responseData.add(responseMonitorItem);
        }
        resultData.put("tableData",responseData);
        return new Result<>(200,resultData,"查询成功！");
    }

    @Override
    public Result deleteMonitorItemById(Integer id) {
        monitorItemMapper.deleteById(id);
        return new Result<>(200,null,"删除成功");
    }

    @Override
    public Result getMonitorItemById(Integer id) {
        MonitorItem monitorItem = monitorItemMapper.selectById(id);

        Host host = hostMapper.selectById(monitorItem.getHostId());
        MonitorType monitorType = monitorTypeMapper.selectById(monitorItem.getMonitorTypeId());

        Map<String,Object> result = new HashMap<>();
        result.put("id",monitorItem.getId());
        result.put("selectMonitorType",monitorType.getName());
        result.put("selectHost",host.getHostName());
        result.put("warnValue",monitorItem.getWarnValue());
        result.put("detail",monitorItem.getDetail());

        return new Result<>(200,result,"查询成功！");
    }

    @Override
    public Result editMonitorItemOne(Map<String, Object> requestData) {
        MonitorItem monitorItem = new MonitorItem();

        LambdaQueryWrapper<Host> lambdaHostQueryWrapper = new LambdaQueryWrapper<>();
        lambdaHostQueryWrapper.eq(requestData.get("selectHost")!=null,Host::getHostName,requestData.get("selectHost"));
        Host host = hostMapper.selectOne(lambdaHostQueryWrapper);

        LambdaQueryWrapper<MonitorType> lambdaMonitorTypeQueryWrapper = new LambdaQueryWrapper<>();
        lambdaMonitorTypeQueryWrapper.eq(requestData.get("selectMonitorType")!=null,MonitorType::getName,requestData.get("selectMonitorType"));
        MonitorType monitorType = monitorTypeMapper.selectOne(lambdaMonitorTypeQueryWrapper);

        Double warnValue = null;
        if (requestData.get("warnValue") != null && !requestData.get("warnValue").equals("")){
            warnValue = Double.parseDouble((requestData.get("warnValue").toString()));
        }
        String id = requestData.get("id").toString();

        monitorItem.setId(Integer.valueOf(id));
        monitorItem.setHostId(host.getId());
        monitorItem.setMonitorTypeId(monitorType.getId());
        monitorItem.setWarnValue(warnValue);
        monitorItem.setDetail((String) requestData.get("detail"));

        monitorItemMapper.updateById(monitorItem);
        return new Result<>(200,null,"修改成功！");
    }
//    @Override
//    public Result<List<MonitorItem>> selectPage(Integer page, Integer size) {
//        Map<String,Object> result = new HashMap<>();
//        IPage<MonitorItem> iPage = new Page(page,size);
//
//
//        IPage<MonitorItem> monitorItemIPage = monitorItemMapper.selectPage(iPage,null);
//        List<MonitorItem> monitorItems = monitorItemIPage.getRecords();
//        List<MonitorItem> monitorItemResults = monitorItems.stream().map(monitorItem -> {
//            String hostName = hostMapper.selectById(monitorItem.getHostId()).getHostName();
//            String monitorTypeName = monitorTypeMapper.selectById(monitorItem.getMonitorTypeId()).getName();
//
//            monitorItemResult monitorItemResult = new monitorItemResult();
//            monitorItemResult.setMonitorTypeName(monitorTypeName);
//            monitorItemResult.setHostName(hostName);
//            monitorAlarmResult.setAlarmTime(monitorAlarm.getAlarmTime());
//            monitorAlarmResult.setData(monitorAlarm.getData());
//            return monitorItemResult;
//        }).collect(Collectors.toList());
//        result.put("tableData",monitorItemResults);
//
//        Integer count = monitorItemMapper.selectCount(null);
//        result.put("total",count);
//        return new Result(200,result,"分页查询成功！");
//    }
}
