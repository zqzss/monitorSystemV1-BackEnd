package com.seewin.service;

import com.seewin.entity.Host;
import com.seewin.entity.Result;

import java.util.HashMap;
import java.util.List;

public interface HostService {
    public Result addHost(Host host);
    public Result<List<Host>> getHostAll(HashMap queryData);
    public Result<Host> updateHost(Host host);
    public Result deleteHost(Integer id);
    public void fetchHostData();

    Result<Host> getHostById(Integer id);

    Result<List<String>> getHostName();
}
