package com.seewin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seewin.entity.*;
import com.seewin.mapper.HostMapper;
import com.seewin.mapper.MonitorDataMapper;
import com.seewin.mapper.MonitorItemMapper;
import com.seewin.mapper.MonitorTypeMapper;
import com.seewin.service.MonitorDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MonitorDataServiceImpl implements MonitorDataService {
    @Autowired
    private MonitorDataMapper monitorDataMapper;
    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private MonitorItemMapper monitorItemMapper;
    @Autowired
    private MonitorTypeMapper monitorTypeMapper;

    @Override
    public void addMonitorData(MonitorData monitorData) {
        log.info("添加监控数据: "+ monitorData);
        monitorDataMapper.insert(monitorData);
    }

    @Override
    public Result getMonitorDataByQuery(HashMap queryData) {
//        返回的data
        Map<String, Object> resultData = new HashMap<>();
//        获取主机名
        String hostName = queryData.get("hostName") != null ? queryData.get("hostName").toString() : null;
//        根据主机名模糊查询主机，获取主机的id
        List<Integer> host_ids = new ArrayList<>();
        LambdaQueryWrapper<MonitorItem> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (hostName != null && !"".equals(hostName)) {
            QueryWrapper<Host> queryWrapper = new QueryWrapper<>();
            queryWrapper.like(!hostName.isEmpty(), "hostName", hostName);
            List<Host> hosts = hostMapper.selectList(queryWrapper);
            if (hosts == null || hosts.isEmpty()) {
                resultData.put("tableData", null);
                resultData.put("total", 0);
                return new Result<>(200, resultData, "查询成功！");
            } else {
                for (Host host : hosts) {
                    host_ids.add(host.getId());
                }
            }
        }
//        获取前端传来的当前页和页码大小
        Integer currentPage = (Integer) queryData.get("currentPage");
        Integer pageSize = (Integer) queryData.get("pageSize");
        log.info("监控数据分页查询getMonitorDataByQuery: [" + "hostName: " + hostName + " currentPage: " + currentPage + " pageSize: " + pageSize +" ]");
//        根据分页、主机id查询监控项
        IPage<MonitorItem> page = new Page(currentPage,pageSize);
        LambdaQueryWrapper<MonitorItem> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(host_ids.isEmpty()!=true,MonitorItem::getHostId,host_ids);
        IPage<MonitorItem> monitorItemIPage = monitorItemMapper.selectPage(page, lambdaQueryWrapper);
        List<MonitorItem> monitorItems = monitorItemIPage.getRecords();

//        得到分页的总页数
        Integer total = Math.toIntExact(monitorItemIPage.getTotal());
        resultData.put("total",total);

//       根据监控项找到最新的监控数据， 返回每个监控项最新的监控数据
        List<MonitorDataResult> monitorDataResults = monitorItems.stream().map(monitorItem1 -> {
//            根据监控项找到最新的监控数据
            LambdaQueryWrapper<MonitorData> monitorDataLambdaQueryWrapper = new LambdaQueryWrapper<>();
            monitorDataLambdaQueryWrapper.eq(MonitorData::getMonitorItemId,monitorItem1.getId());
            monitorDataLambdaQueryWrapper.orderByDesc(MonitorData::getCreateTime);
            monitorDataLambdaQueryWrapper.last("limit 1");
            MonitorData monitorData = monitorDataMapper.selectOne(monitorDataLambdaQueryWrapper);
            if (monitorData == null){
                return null;
            }

//            生成返回的监控数据
            MonitorDataResult monitorDataResult = new MonitorDataResult();
            String hostName2 = hostMapper.selectById(monitorItem1.getHostId()).getHostName();
            monitorDataResult.setHostName(hostName2);
            monitorDataResult.setData(monitorData.getData());
            monitorDataResult.setDetail(monitorItem1.getDetail());
            String MonitorTypeName = monitorTypeMapper.selectById(monitorItem1.getMonitorTypeId()).getName();
            monitorDataResult.setMonitorTypeName(MonitorTypeName);
            monitorDataResult.setCreateTime(monitorData.getCreateTime());

            return monitorDataResult;
        }).collect(Collectors.toList());
        resultData.put("tableData",monitorDataResults);
        log.info("监控数据分页查询条件: 【"+"hostName: "+hostName+", currentPage+: "+currentPage+", pageSize: "+pageSize+"】");
        log.info("监控数据分页查询结果: "+resultData);
        return new Result<>(200,resultData,"查询成功！");
    }

}
