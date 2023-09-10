package com.seewin.controller;

import com.seewin.entity.Host;
import com.seewin.entity.Result;
import com.seewin.common.BusinessException;
import com.seewin.service.HostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/host")
public class HostController {
    @Autowired
    private HostService hostService;
    @PostMapping
    public Result HostAdd(@RequestBody Host host){

        return hostService.addHost(host);
    }
    @GetMapping("/query")
    public Result<List<Host>> getHostAll(@RequestParam HashMap queryData){

        return hostService.getHostAll(queryData);
    }
    @GetMapping("/{id}")
    public Result<Host> getHostById(@PathVariable Integer id){
        return hostService.getHostById(id);
    }
    @GetMapping("/name")
    public Result<List<String>> getHostName(){
        return hostService.getHostName();
    }
    @PutMapping
    public Result updateHost(@RequestBody Host host){
        log.info(host.toString());
        return hostService.updateHost(host);
    }
    @DeleteMapping("/{id}")
    public Result deleteHost(@PathVariable Integer id){
        System.out.println(id);
        return hostService.deleteHost(id);
    }
    @GetMapping("/test")
    public Result test(){
        throw new BusinessException(404,"未找到");
    }
}
