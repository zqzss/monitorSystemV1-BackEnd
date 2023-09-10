package com.seewin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitorAlarmQuery extends  MonitorAlarm{
    private String hostName;
    private List<String> timeRange;
    private List<String> dateRange;
}
