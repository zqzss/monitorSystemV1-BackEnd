package com.seewin.service;

import com.seewin.entity.MonitorAlarm;
import com.seewin.entity.Result;

import java.util.HashMap;
import java.util.List;

public interface MonitorAlarmService {
    public Result<List<MonitorAlarm>> selectPage(Integer page, Integer size);
    public Result<List<MonitorAlarm>> MonitorAlarmQuery(HashMap queryData);
}
