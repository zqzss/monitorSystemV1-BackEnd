package com.seewin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seewin.entity.*;
import com.seewin.mapper.HostMapper;
import com.seewin.mapper.MonitorAlarmMapper;
import com.seewin.mapper.MonitorItemMapper;
import com.seewin.mapper.MonitorTypeMapper;
import com.seewin.service.MonitorAlarmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MonitorAlarmServiceImpl implements MonitorAlarmService {
    @Autowired
    private MonitorAlarmMapper monitorAlarmMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private MonitorTypeMapper monitorTypeMapper;
    @Autowired
    private MonitorItemMapper monitorItemMapper;

    @Override
//    暂时未使用的接口
    public Result<List<MonitorAlarm>> selectPage(Integer page, Integer size) {
        Map<String, Object> result = new HashMap<>();
        IPage<MonitorAlarm> iPage = new Page(page, size);

        LambdaQueryWrapper<MonitorAlarm> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(MonitorAlarm::getAlarmTime);

        IPage<MonitorAlarm> monitorAlarmIPage = monitorAlarmMapper.selectPage(iPage, lambdaQueryWrapper);
        List<MonitorAlarm> monitorAlarms = monitorAlarmIPage.getRecords();
        List<MonitorAlarmResult> monitorAlarmResults = monitorAlarms.stream().map(monitorAlarm -> {
            String hostName = hostMapper.selectById(monitorAlarm.getHostId()).getHostName();
            String monitorTypeName = monitorTypeMapper.selectById(monitorAlarm.getMonitorTypeId()).getName();

            MonitorAlarmResult monitorAlarmResult = new MonitorAlarmResult();
            monitorAlarmResult.setMonitorTypeName(monitorTypeName);
            monitorAlarmResult.setHostName(hostName);
            monitorAlarmResult.setAlarmTime(monitorAlarm.getAlarmTime());
            monitorAlarmResult.setData(monitorAlarm.getData());
            return monitorAlarmResult;
        }).collect(Collectors.toList());
        result.put("tableData", monitorAlarmResults);

        Integer count = monitorAlarmMapper.selectCount(null);
        result.put("total", count);
        return new Result(200, result, "分页查询成功！");
    }

    @Override
    public Result MonitorAlarmQuery(HashMap queryData) {
        Map<String, Object> resultData = new HashMap<>();

//        查询主机名包含某个字符串的主机
        String hostName = queryData.get("hostName") != null ? queryData.get("hostName").toString() : null;
        List<Integer> host_ids = new ArrayList<>();
        LambdaQueryWrapper<MonitorAlarm> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (!hostName.isEmpty()) {
            QueryWrapper<Host> queryWrapper = new QueryWrapper<>();
            queryWrapper.like(!hostName.isEmpty(), "hostName", hostName);
            List<Host> hosts = hostMapper.selectList(queryWrapper);
            if (hosts == null || hosts.isEmpty()) {
                resultData.put("tableData", null);
                resultData.put("total", 0);
                return new Result<>(200, resultData, "查询成功！");
            }
            else {
                for (Host host : hosts) {
                        host_ids.add(host.getId());
                }
            }

        }

//       获取前端传来的日期时间范围，并提取开始日期和结束日期
        ArrayList<String> dateRange = new ArrayList<>();
        if (queryData.get("dateRange") instanceof String) {
            // 单个字符串处理逻辑，例如将其分割并添加到 ArrayList 中
            String[] timeRangeArr = ((String) queryData.get("dateRange")).split("");
            dateRange = new ArrayList<>(Arrays.asList(timeRangeArr));
        } else if (queryData.get("dateRange") instanceof ArrayList) {
            // 已经是 ArrayList 类型，无需转换
            dateRange = (ArrayList<String>) queryData.get("dateRange");
        }
        String startDate = new String();
        String endDate = new String();
        if (dateRange.size() == 2) {
            String startDateTime = dateRange.get(0);
            String endDateTime = dateRange.get(1);
//            System.out.println("startDateTime: "+startDateTime);
            LocalDateTime dateTime1 = LocalDateTime.parse(startDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime dateTime2 = LocalDateTime.parse(endDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            startDate = dateTime1.toLocalDate().toString();
            endDate = dateTime2.toLocalDate().toString();
        }
//        获取前端传来的日期时间范围，并提取开始时间和结束时间
        ArrayList<String> timeRange = new ArrayList<>();
        if (queryData.get("timeRange") instanceof String) {
            // 单个字符串处理逻辑，例如将其分割并添加到 ArrayList 中
            String[] timeRangeArr = ((String) queryData.get("timeRange")).split("");
            timeRange = new ArrayList<>(Arrays.asList(timeRangeArr));
        } else if (queryData.get("timeRange") instanceof ArrayList) {
            // 已经是 ArrayList 类型，无需转换
            timeRange = (ArrayList<String>) queryData.get("timeRange");
        }
        String startTime = new String();
        String endTime = new String();
        if (timeRange.size() == 2) {
            String startDateTime = timeRange.get(0);
            String endDateTime = timeRange.get(1);
            LocalDateTime dateTime1 = LocalDateTime.parse(startDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime dateTime2 = LocalDateTime.parse(endDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            startTime = dateTime1.toLocalTime().toString();
            endTime = dateTime2.toLocalTime().toString();
        }

//        生成查询数据库的开始日期时间
        LocalDateTime localDateTime = LocalDateTime.now();
        String queryStartDateTime = new String();
        if (!startTime.isEmpty() && !startDate.isEmpty()) {
            queryStartDateTime = startDate + " " + startTime;
        } else if (startTime.isEmpty() && !startDate.isEmpty()) {
            queryStartDateTime = startDate + " " + "00:00:00";
        } else if (!startTime.isEmpty() && startDate.isEmpty()) {
//            System.out.println("queryStartDateTime: "+queryStartDateTime);
            queryStartDateTime = "1970-00-00" + " " + startTime;
        } else {
            queryStartDateTime = null;
        }

//      生成查询数据库的结束日期时间
        String queryEndDateTime = new String();
        if (!endDate.isEmpty() && !endTime.isEmpty()) {
            queryEndDateTime = endDate + " " + endTime;
        } else if (endDate.isEmpty() && !endTime.isEmpty()) {
            queryEndDateTime = localDateTime.toLocalDate() + "" + endTime;
        } else if (!endDate.isEmpty() && endTime.isEmpty()) {
            queryEndDateTime = endDate + "" + "23:59:59";
        } else {
            queryEndDateTime = null;
        }

//      获取前端传来的页面大小和当前页码，分页查询处理
        Integer currentPage = (Integer) queryData.get("currentPage");
        Integer pageSize = (Integer) queryData.get("pageSize");
        Integer offSet = (currentPage - 1) * pageSize;
        MonitorItem monitorItem = new MonitorItem();

//        对分页及条件查询的对象做处理，生成返回给前端的MonitorAlarmResult对象
        List<MonitorAlarm> monitorAlarms = monitorAlarmMapper.selectMonitorAlarmByQuery(host_ids, queryStartDateTime, queryEndDateTime, pageSize, offSet,startTime,endTime,startDate,endDate);
        List<MonitorAlarmResult> monitorAlarmResults = monitorAlarms.stream().map(monitorAlarm -> {
            String hostName2 = hostMapper.selectById(monitorAlarm.getHostId()).getHostName();


            String monitorTypeName = monitorTypeMapper.selectById(monitorAlarm.getMonitorTypeId()).getName();
            String detail = monitorItemMapper.selectById(monitorAlarm.getMonitorItemId()).getDetail();

            MonitorAlarmResult monitorAlarmResult = new MonitorAlarmResult();
            monitorAlarmResult.setMonitorTypeName(monitorTypeName);
            monitorAlarmResult.setHostName(hostName2);
            monitorAlarmResult.setDetail(detail);

            String alarmTime = monitorAlarm.getAlarmTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = null;
            try {
                parse = dateFormat.parse(alarmTime);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            String alarmTimeAsString = dateFormat.format(parse);
            monitorAlarmResult.setAlarmTime(alarmTimeAsString);

            monitorAlarmResult.setData(monitorAlarm.getData());
            return monitorAlarmResult;
        }).collect(Collectors.toList());
        resultData.put("tableData", monitorAlarmResults);

        Integer count = monitorAlarmMapper.selectMonitorAlarmOfCount(host_ids, queryStartDateTime, queryEndDateTime,startTime,endTime,startDate,endDate);
        resultData.put("total", count);
        log.info("告警记录分页查询条件: 【"+ "hostName: "+hostName+", dateRange" + dateRange +", timeRange: "+timeRange+", currentPage: "+ currentPage+", pageSize: "+ pageSize+"】");
        log.info("告警记录分页查询结果: "+resultData);
        Result result = new Result<>(200, resultData, "查询成功！");
        return result;
    }
}
