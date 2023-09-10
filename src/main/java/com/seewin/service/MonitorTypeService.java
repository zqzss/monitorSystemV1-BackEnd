package com.seewin.service;

import com.seewin.entity.Result;

import java.util.List;

public interface MonitorTypeService {
    public Result<List<String>> getAllMonitorTypeName();
}
