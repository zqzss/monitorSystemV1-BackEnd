package com.seewin.service;

import com.seewin.entity.MonitorData;
import com.seewin.entity.Result;

import java.util.HashMap;

public interface MonitorDataService {
    public void addMonitorData(MonitorData monitorData);
    public Result getMonitorDataByQuery(HashMap queryData);
}
