package com.seewin.service;

import com.seewin.entity.Result;

import java.util.HashMap;
import java.util.Map;

public interface MonitorItemService {
    public Result addMonitorItem(Map<String,Object> requestData);

    public Result getAllMonitorItem(HashMap queryData);
    public Result deleteMonitorItemById(Integer id);

    Result getMonitorItemById(Integer id);

    Result editMonitorItemOne(Map<String, Object> requestData);

//    public Result<List<MonitorItem>> selectPage(Integer page, Integer size);
}
