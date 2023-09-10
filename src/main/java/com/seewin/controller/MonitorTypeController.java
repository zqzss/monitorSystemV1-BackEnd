package com.seewin.controller;

import com.seewin.entity.Result;
import com.seewin.service.MonitorItemService;
import com.seewin.service.MonitorTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monitorType")
public class MonitorTypeController {
    @Autowired
    private MonitorTypeService monitorTypeService;
    @Autowired
    private MonitorItemService monitorItemService;
    @GetMapping("/name")
    public Result<List<String>> getAllMonitorTypeName(){
        return monitorTypeService.getAllMonitorTypeName();
    }

}
