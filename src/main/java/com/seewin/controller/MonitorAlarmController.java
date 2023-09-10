package com.seewin.controller;

import com.seewin.entity.Result;
import com.seewin.service.MonitorAlarmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@Slf4j
@RequestMapping("/monitorAlarm")
public class MonitorAlarmController {
    @Autowired
    private MonitorAlarmService monitorAlarmService;
    @GetMapping
    //    暂时未使用的接口
    public Result MonitorAlarmPage(@RequestParam Integer page,@RequestParam Integer pageSize){
        return monitorAlarmService.selectPage(page,pageSize);
    }
    @PostMapping ("/query")
//    public Result MonitorAlarmQuery(@RequestParam String hostName, @RequestParam(required = false) List<String> dateRange, @RequestParam(required = false) List<String> timeRange){
    public Result MonitorAlarmQuery(@RequestBody(required = false) HashMap queryData){

        System.out.println(queryData);
        return monitorAlarmService.MonitorAlarmQuery(queryData);
    }
}
