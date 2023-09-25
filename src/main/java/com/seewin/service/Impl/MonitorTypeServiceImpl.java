package com.seewin.service.Impl;

import com.seewin.entity.MonitorType;
import com.seewin.entity.Result;
import com.seewin.mapper.MonitorTypeMapper;
import com.seewin.service.MonitorTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MonitorTypeServiceImpl implements MonitorTypeService {
    @Autowired
    private MonitorTypeMapper monitorTypeMapper;
    @Override
    public Result<List<String>> getAllMonitorTypeName() {
        List<MonitorType> monitorTypes = monitorTypeMapper.selectList(null);
        List<String> monitorTypeData = new ArrayList<>();
        for(MonitorType monitorType:monitorTypes){
            monitorTypeData.add(monitorType.getName());
        }
        log.info("获取所有监控类型名称: "+monitorTypeData);
        return new Result<>(200,monitorTypeData,"查询成功！");
    }
}
