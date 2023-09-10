package com.seewin.controller;

import com.seewin.entity.Result;
import com.seewin.service.MonitorDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/monitorData")
public class MonitorDataController {
    @Autowired
    private MonitorDataService monitorDataService;
    @PostMapping
    public Result getMonitorDataByQuery(@RequestBody HashMap queryData){
        return  monitorDataService.getMonitorDataByQuery(queryData);
    }
}
