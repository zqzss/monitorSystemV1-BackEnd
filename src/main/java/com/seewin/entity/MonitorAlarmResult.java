package com.seewin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitorAlarmResult {
    private Integer id;
    private String hostName;
    private String monitorTypeName;
    private String data;
    private String alarmTime;
    private String detail;
}
