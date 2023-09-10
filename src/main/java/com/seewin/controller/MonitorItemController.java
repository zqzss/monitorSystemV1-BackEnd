package com.seewin.controller;


import com.seewin.entity.Result;
import com.seewin.service.MonitorItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/monitorItem")
public class MonitorItemController {
    @Autowired
    private MonitorItemService monitorItemService;
    @PostMapping
    public Result addMonitorItem(@RequestBody Map<String,Object> requestData){

        return monitorItemService.addMonitorItem(requestData);
    }
    @GetMapping
    public Result getAllMonitorItem(@RequestParam HashMap queryData){
        return monitorItemService.getAllMonitorItem(queryData);
    }
    @GetMapping("/{id}")
    public Result getMonitorItemById(@PathVariable Integer id){
        return monitorItemService.getMonitorItemById(id);
    }
    @DeleteMapping("/{id}")
    public Result deleteMonitorItemById(@PathVariable Integer id){

        return monitorItemService.deleteMonitorItemById(id);
    }
    @PutMapping
    public Result editMonitorItemOne(@RequestBody Map<String,Object> requestData){
        return monitorItemService.editMonitorItemOne(requestData);
    }
}
